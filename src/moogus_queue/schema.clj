(ns moogus-queue.schema)

(def schema
  [{:namespace :queue-entry
    :attrs [[:message :string]
            [:attempted-count :long]
            [:issuccessful :boolean]]}])
