(ns moogus-queue.testlib
  (:require [compojure.core :refer [POST]]
            [ring.adapter.jetty :refer [run-jetty]] 
            [ring.middleware.params :refer [wrap-params]]
            [moogus-queue]))

(def genius-well (atom nil))

(defn -testing-api [_]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Powered by Message"})

(def testing-api
  (wrap-params
   (POST "/:function" [function]
         (fn testing-api- [req]
           (spit "/tmp/well.edn" req :append true)
           (swap! genius-well conj {:function function :query-params (:query-params req)})
           "replace me with fake Genius XML"))))

(defn testing-fixture [f]
  (let [j (run-jetty testing-api {:port 8083 :join? false})]
    (swap! genius-well (constantly #{}))
    (f)
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
