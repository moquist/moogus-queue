(ns moogus-queue.web-test
  (:require [clojure.test :refer :all]
            [immutant.dev]
            [immutant.web]
            [datomic.api :as d]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :as tct]
            [moogus-queue]
            [moogus-queue.web]
            [moogus-queue.testlib :as mqt]))

(def num-quick-checks 1000)

(defn testing-fixture [f]
  (swap! moogus-queue/system moogus-queue/start-system!)
  (f)
  (swap! moogus-queue/system moogus-queue/stop-system!))

(use-fixtures :once testing-fixture)

(tct/defspec web-assert-queue-entry-test2
  num-quick-checks
  (prop/for-all
   [msg gen/string]
   (let [db-conn (:db-conn @moogus-queue/system)
         t (moogus-queue.web/assert-queue-entry db-conn msg)]
     (and (mqt/ensure-tx t)
          (integer? (ffirst
                     (d/q '[:find ?e
                            :in $ ?data
                            :where [?e :queue-entry/message ?data]]
                          (d/db db-conn)
                          msg)))))))

