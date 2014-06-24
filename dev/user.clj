(ns user
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer (pprint)]
            [clojure.repl :refer :all]
            [clojure.test :as test]
            [datomic.api :as d]
            [datomic-schematode :as dst]
            [immutant.dev]
            [moogus-queue]))

(defn stop! []
  (alter-var-root #'moogus-queue/system moogus-queue/stop-system!))

(defn start! []
  (alter-var-root #'moogus-queue/system moogus-queue/start-system!))

(defn reset
  "If you are accustomed to tools.namespace and reset, you can use this.
   It makes you feel better."
  []
  (stop!)
  (immutant.dev/reload-project!)
  (start!))

(defn reset-and-delete-db! [delete-db]
  (when (= :delete-db delete-db)
    (stop!)
    (d/delete-database (get-in moogus-queue/system [:config :datomic-url]))
    (immutant.dev/reload-project!)
    (start!)))

(defn touch-that
  "Execute the specified query on the current DB and return the
   results of touching each entity.

   The first binding must be to the entity.
   All other bindings are ignored."
  [query & data-sources]
  (map #(d/touch
         (d/entity
          (d/db (:db-conn moogus-queue/system))
          (first %)))
       (apply d/q query (d/db (:db-conn moogus-queue/system)) data-sources)))

(defn ptouch-that
  "Example: (ptouch-that '[:find ?e :where [?e :user/username]])"
  [query & data-sources]
  (pprint (apply touch-that query data-sources)))

(comment
  (ptouch-that '[:find ?e :where [?e :comp/name]])
  
  )

