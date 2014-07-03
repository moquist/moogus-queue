(ns moogus-queue
  (:require
   [immutant.web]
   [immutant.messaging]
   [clj-http.client]
   [datomic-schematode :as dst]
   [datomic.api :as d]
   [moogus-queue.web]
   [moogus-queue.schema])
  (:import (java.io File)))

(def queue-name "queue.moogus")
(def system (atom nil))

(defn file-exists? [path]
  (if (.isFile (File. path)) true false))

(defn load-system-config []
  (let [file (immutant.util/app-relative "moogus-queue-conf.edn")]
    (clojure.edn/read-string (slurp file))))

(defn call! [uri f params]
  (let [url (str uri "/" f)]
    (clj-http.client/post url {:form-params params})))

(defn worker [{:keys [genius-api-url genius-api-token-outgoing]} message]
  (let [f (:function message)
        params (dissoc message :function)
        params (assoc message :token genius-api-token-outgoing)]
    (spit "/tmp/blarpfiddle.edn" (call! genius-api-url f params))))

(defn start-queue! [system]
  (immutant.messaging/start queue-name)
  (immutant.messaging/listen queue-name (partial worker (:config system))))

(defn start-system! [system]
  (let [system (if (nil? system)
                 {:config (load-system-config)}
                 system)
        db-url (-> system :config :db-url)
        system (assoc system :queue-name queue-name)
        system (assoc system :new-db (d/create-database db-url))
        system (assoc system :db-conn (d/connect db-url))]
    ;;(ring.adapter.jetty/run-jetty moogus-queue.web/app {:port 9000 :join? false})
    (dst/load-schema! (:db-conn system) moogus-queue.schema/schema)
    (start-queue! system)
    (immutant.web/start (moogus-queue.web/app system))
    (assoc system
      :immutant-queue true
      :immutant-web true)))

(defn stop-system! [system]
  (immutant.web/stop)
  (immutant.messaging/stop queue-name :force true)
  nil)

(defn init []
  (swap! system start-system!))
