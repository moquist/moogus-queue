(ns moogus-queue.web
  (:require [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            ;;[ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes PUT]]))

(defresource queue
  :allowed-methods [:put]
  :available-media-types ["application/json"]
  :put! (fn [ctx] "here be my thing")
  :handle-created (fn [ctx] (str ctx)))

(defroutes routes
  (PUT "/" [] queue))

(def app
  (-> routes
      (wrap-params)
      (wrap-trace :header :ui)
      ))
