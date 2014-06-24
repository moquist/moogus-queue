(ns moogus-queue.web-test
  (:require [clojure.test :refer :all]
            [immutant.dev]
            [immutant.web]
            [datomic.api :as d]
            [moogus-queue]
            [moogus-queue.web]
            [moogus-queue.testlib :as mqt]))

(defn testing-fixture [f]
  (alter-var-root #'moogus-queue/system moogus-queue/start-system!)
  (f)
  (alter-var-root #'moogus-queue/system moogus-queue/stop-system!))

(use-fixtures :once testing-fixture)

(deftest web-assert-queue-entry
  (let [db-conn (:db-conn moogus-queue/system)
        data "womp-a-domp"
        t (moogus-queue.web/assert-queue-entry db-conn data)]
    (testing "assert-queue-entry"
      (is (mqt/ensure-tx t))
      (is (integer? (ffirst
                     (d/q '[:find ?e
                            :in $ ?data
                            :where [?e :queue-entry/message ?data]]
                          (d/db db-conn)
                          data)))))))
