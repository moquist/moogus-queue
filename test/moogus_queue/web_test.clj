(ns moogus-queue.web-test
  (:require [clojure.test :refer :all]
            [immutant.dev]
            [immutant.web]
            [datomic.api :as d]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [moogus-queue]
            [moogus-queue.web]
            [moogus-queue.testlib :as mqt]))

(def num-quick-checks 1000)

(defn testing-fixture [f]
  (alter-var-root #'moogus-queue/system moogus-queue/start-system!)
  (f)
  (alter-var-root #'moogus-queue/system moogus-queue/stop-system!))

(use-fixtures :once testing-fixture)

(deftest web-assert-queue-entry-test
  (let [db-conn (:db-conn moogus-queue/system)
        prop (prop/for-all
              [msg gen/string]
              (let [t (moogus-queue.web/assert-queue-entry db-conn msg)]
                (testing "assert-queue-entry"
                  (is (mqt/ensure-tx t))
                  (is (integer? (ffirst
                                 (d/q '[:find ?e
                                        :in $ ?data
                                        :where [?e :queue-entry/message ?data]]
                                      (d/db db-conn)
                                      msg)))))))
        t (tc/quick-check num-quick-checks prop)]
    (is (:result t) (str t))))
