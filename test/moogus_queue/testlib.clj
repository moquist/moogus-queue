(ns moogus-queue.testlib
  (:require [moogus-queue]))

(def num-quick-checks 1000)

(defn testing-fixture [f]
  (swap! moogus-queue/system moogus-queue/start-system!)
  (f)
  (swap! moogus-queue/system moogus-queue/stop-system!))

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
