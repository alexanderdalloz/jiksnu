(ns jiksnu.routes.webfinger-routes-test
  (:use [ciste.config :only [config]]
        [ciste.model :only [fetch-resource query string->document]]
        [jiksnu.model :only [rel-filter]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [every-checker fact]])
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [ring.mock.request :as mock]))

(test-environment-fixture

 (fact "Requesting the host meta"
   (fact "returns the host meta as xml"
     (->> "/.well-known/host-meta"
          (mock/request :get)
          response-for) =>
          (every-checker
           #(= 200 (:status %))
           #(= "application/xrds+xml" (-> % :headers (get "Content-Type")))
           #(let [body (string->document (:body %))]
              (and
               ;; has at least 1 lrdd link
               (seq (query "//*[local-name() = 'Link'][@rel = 'lrdd']" body)))))))

 (fact "host meta json"
   (->> "/.well-known/host-meta.json"
        (mock/request :get)
        response-for) =>
        (every-checker
         #(= 200 (:status %))
         #(= "application/json" (-> % :headers (get "Content-Type")))
         #(let [body (json/read-json (:body %))]
            (and
             ;; has at least 1 link
             (>= 1 (count (:links body)))

             ;; host property matches domain
             ;; NB: this will probably be removed
             (= (config :domain) (:host body))

             ;; has a lrdd link
             (rel-filter "lrdd" (:links body))))))

 )