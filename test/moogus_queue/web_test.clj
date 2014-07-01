(ns moogus-queue.web-test
  (:require [clojure.test :refer :all]
            [immutant.dev]
            [immutant.web]
            [immutant.util]
            [datomic.api :as d]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :as tct]
            [clojure.data.json :as json]
            [clj-http.client]
            [moogus-queue]
            [moogus-queue.web]
            [moogus-queue.testlib :as mqt]))

(use-fixtures :once mqt/testing-fixture)

(tct/defspec assert-queue-entry-test
  1000
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

(tct/defspec call-myself
  1000
  (prop/for-all
   [f (gen/such-that not-empty gen/string-ascii)
    m (gen/such-that
       not-empty
       (gen/map gen/keyword
                (gen/fmap
                 ring.util.codec/url-encode
                 (gen/such-that not-empty gen/string-ascii))))]
   (let [url (immutant.util/app-uri)
         r (clj-http.client/put
            url
            {:body (json/write-str (assoc m :function f))})]
     (= 201 (:status r)) (str r))))

