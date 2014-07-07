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
         entid (moogus-queue.web/assert-queue-entry db-conn msg)]
     (= msg
        (ffirst
         (d/q '[:find ?data
                :in $ ?e
                :where [?e :queue-entry/message ?data]]
              (d/db db-conn)
              entid))))))

(defn check-expected
  "Returns true if our fake \"genius\" receive the call and data we sent." 
  [expected]
  (= expected @moogus-queue.testlib/genius-well))

(defn token->header [token]
  (str "Token " token))

(tct/defspec call-myself
  1000
  (prop/for-all
   [f (gen/fmap ring.util.codec/url-encode
                (gen/such-that not-empty gen/string-ascii))
    m (gen/such-that not-empty
                     (gen/map gen/keyword
                              (gen/fmap ring.util.codec/url-encode
                                        (gen/such-that not-empty gen/string-ascii))))]
   (let [expected {:function f :query-params m}
         url (immutant.util/app-uri)
         token (token->header (-> @moogus-queue/system
                                  :config
                                  :api-token-incoming))
         r (clj-http.client/put
            url
            {:body (json/write-str (assoc m :function f))
             :headers {:authorization token}})]
     (and (= 201 (:status r))
          (check-expected expected)) (str r))))


