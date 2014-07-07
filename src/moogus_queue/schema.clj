(ns moogus-queue.schema)

(def schema
  [{:namespace :queue-entry
    :attrs [[:message :string]
            [:attempted-count :long]
            [:http-status :long :many]
            [:genius-response-full :string :many]
            [:genius-response-msg :string :many]]}])
