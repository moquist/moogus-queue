(ns moogus-queue.web
  (:require [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            ;;[ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [routes PUT]]
            [clojure.data.json :as json]
            [immutant.messaging]
            [datomic.api :as d]
            [moogus-queue.auth :as auth]))

(defn assert-queue-entry [db-conn message]
  (let [tx (d/transact db-conn [{:db/id (d/tempid :db.part/user -1)
                                 :queue-entry/message (str message)
                                 :queue-entry/attempted-count 0}])
        {:keys [tempids db-after]} @tx]
    (d/resolve-tempid db-after tempids (d/tempid :db.part/user -1))))

(defn enqueue [system ctx]
  (let [queue-name (:queue-name system)
        body (slurp (get-in ctx [:request :body]))
        body (json/read-str body :key-fn keyword)
        entid (assert-queue-entry (:db-conn system) body)]
    (immutant.messaging/publish queue-name
                                {:entid entid :message body})))

(defresource qresource [system _request]
  :allowed-methods [:put]
  :available-media-types ["application/json"]
  :put! (partial enqueue system)
  :authorized? (partial auth/validate-token (:config system))
  :handle-unauthorized "You are not authorized to access this resource.")

(defn app [system]
  (-> (routes (PUT "/" [] (partial qresource system)))
      (wrap-params)
      (wrap-trace :header :ui)
      ))
