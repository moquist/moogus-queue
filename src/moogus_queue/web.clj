(ns moogus-queue.web
  (:require [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            ;;[ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [routes PUT]]
            [clojure.data.json :as json]
            [immutant.messaging]
            [datomic.api :as d]))

(defn assert-queue-entry [db-conn message]
  (d/transact db-conn [{:db/id (d/tempid :db.part/user)
                        :queue-entry/message (str message)
                        :queue-entry/attempted-count 0}]))

(defn enqueue [system ctx]
  (let [queue-name (:queue-name system)
        body (slurp (get-in ctx [:request :body]))
        body (json/read-str body :key-fn keyword)]
    (when (assert-queue-entry (:db-conn system) body)
      (immutant.messaging/publish queue-name body))))

(defresource qresource [system _request]
  :allowed-methods [:put]
  :available-media-types ["application/json"]
  :put! (partial enqueue system))

(defn app [system]
  (-> (routes (PUT "/" [] (partial qresource system)))
      (wrap-params)
      (wrap-trace :header :ui)
      ))
