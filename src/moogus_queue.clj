(ns moogus-queue
  (:require
   [immutant.web]
   ;;[ring.adapter.jetty]
   [moogus-queue.web])
  (:import (java.io File)))

(def system nil)

(defn file-exists? [path]
  (if (.isFile (File. path)) true false))

(defn load-system-config []
  (let [path "moogus-queue-conf.edn"]
    (if (file-exists? path)
      (clojure.edn/read-string (slurp path))
      (throw (Exception. (str "Config file missing: " path))))))

(defn start-system! [_]
  (let [system (load-system-config)]
    ;;(ring.adapter.jetty/run-jetty moogus-queue.web/app {:port 9000 :join? false})
    (immutant.web/start moogus-queue.web/app)
    (assoc system :immutant-web true)))

(defn stop-system! [_])

(defn init []
  (alter-var-root #'system start-system!))
