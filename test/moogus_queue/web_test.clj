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

(use-fixtures :once mqt/testing-fixture)

(tct/defspec assert-queue-entry-test
  mqt/num-quick-checks
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

