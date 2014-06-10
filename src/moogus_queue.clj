(ns moogus-queue
  (:require
   [immutant.web]
   [immutant.messaging]
   ;;[ring.adapter.jetty]
   [moogus-queue.web])
  (:import (java.io File)))

(def queue-name "queue.moogus")
(def system nil)

(defn file-exists? [path]
  (if (.isFile (File. path)) true false))

(defn load-system-config []
  (let [path "moogus-queue-conf.edn"]
    (if (file-exists? path)
      (clojure.edn/read-string (slurp path))
      (throw (Exception. (str "Config file missing: " path))))))

(defn worker [system message] (spit "/tmp/blarpfiggle.edn" message :append true))

(defn start-queue! [system]
  (immutant.messaging/start queue-name)
  (immutant.messaging/listen queue-name (partial worker system)))

(defn start-system! [_]
  (let [system (load-system-config)
        system (assoc system :queue-name queue-name)]
    ;;(ring.adapter.jetty/run-jetty moogus-queue.web/app {:port 9000 :join? false})
    (start-queue! system)
    (immutant.web/start (moogus-queue.web/app system))
    (assoc system
      :immutant-queue true
      :immutant-web true)))

(defn stop-system! [_])

(defn init []
  (alter-var-root #'system start-system!))
