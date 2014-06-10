(ns moogus-queue.web
  (:require [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            ;;[ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [routes PUT]]
            [clojure.data.json :as json]
            [immutant.messaging]))

(defn enqueue [system ctx]
  (let [queue-name (:queue-name system)
        body (slurp (get-in ctx [:request :body]))
        body (json/read-str body :key-fn keyword)]
    (immutant.messaging/publish queue-name body)))

(defresource qresource [system _request]
  :allowed-methods [:put]
  :available-media-types ["application/json"]
  :put! (partial enqueue system)
  :handle-created (fn [ctx] (str ctx)))

(defn app [system]
  (-> (routes (PUT "/" [] (partial qresource system)))
      (wrap-params)
      (wrap-trace :header :ui)
      ))
