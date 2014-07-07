(ns moogus-queue.auth
  (:require [clojure.string :as string]
            [clj-time.core :as t]))

(defn request->auth-token
  "Retrieves the authentication token from an incoming
  request.

  Params:  ctx - Liberator Context
  Returns: string"
  [ctx]
  (let [auth (get-in ctx [:request :headers "authorization"])]
    (if (nil? auth)
      (str "")
      (let [split-auth (string/split auth #"\s+")]
        (if (= (str "Token") (first split-auth))
          (second split-auth)
          (str ""))))))

(defn gen-token
  "Generates a random token of a specified length consisting
  of numbers, letters, and symbols.

  Params:  length - int
  Returns: string"
  [length]
  (apply str
         (take length
               (repeatedly
                #(rand-nth "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*()-_")))))

(defn validate-token
  "Validates a token from an incoming request,
  designed to be used in the authorized? section
  of a liberator resource.

  Params:  ctx - Liberator Context
  db-conn  - Datomic database connection
  Returns: boolean"
  [config ctx]
  (let [token  (request->auth-token ctx)]
    (= token (:api-token-incoming config))))
