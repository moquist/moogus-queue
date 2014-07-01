(ns moogus-queue.testlib
  (:require [compojure.core :refer [POST]]
            [ring.adapter.jetty :refer [run-jetty]] 
            [moogus-queue]))

(def num-quick-checks 1000)

(def genius-well (atom []))

(defn -testing-api [_]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Powered by Message"})

(def testing-api
  (POST "/:function" [f] (fn testing-api- [req] (swap! genius-well conj req) "hi there!")))

(defn testing-fixture [f]
  (let [j (run-jetty testing-api {:port 8081 :join? false})]
    (swap! genius-well (constantly []))
    (swap! moogus-queue/system moogus-queue/start-system!)
    (f)
    (swap! moogus-queue/system moogus-queue/stop-system!)
    (.stop j)))

(defmacro should-throw
  "Borrowed from https://github.com/Datomic/day-of-datomic .
   Runs forms, expecting an exception. Returns exception message if an
   exception occurred, and false if no exception occurred."
      [& forms]
      `(try
         ~@forms
         false
         (catch Exception t#
           (str "Got expected exception:\n\t" (.getMessage t#)))))

(defn ensure-tx [tx]
  (= '(:db-after :db-before :tempids :tx-data) (sort (keys @tx))))

(defn ensure-seq-txs [txs]
  (every? ensure-tx txs))

(defn predict-entity-map [e]
  (merge (into {} e) {:db/id -1}))
