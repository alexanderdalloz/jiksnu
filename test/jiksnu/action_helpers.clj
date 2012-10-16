(ns jiksnu.action-helpers
  (:use [aleph.formats :only [channel-buffer->string]]
        [aleph.http :only [sync-http-request]]
        [clj-webdriver.taxi :only [to]]
        [lamina.core :only [permanent-channel]]
        [midje.sweet :only [fact]])
  (:require [clojure.tools.logging :as log]))

(def page-names
  {
   "home"                           "/"
   "login"                          "/main/login"
   "ostatus sub"                    "/main/ostatussub"
   "host-meta"                      "/.well-known/host-meta"
   "subscription index"             "/admin/subscriptions"
   "edit profile"                   "/main/profile"
   "user admin"                     "/admin/users"
   "user index"                     "/users"
   "domain index"                   "/main/domains"
   "feed source admin index"        "/admin/feed-sources"
   "feed subscriptions admin index" "/admin/feed-subscriptions"
   "like admin index"               "/admin/likes"
   "subscriptions admin index"      "/admin/subscriptions"
   "firehose"                       "/main/events"})

(def current-page (ref nil))
(def domain "localhost")
(def port 8175)
(def that-stream (permanent-channel))

(defn expand-url
  [path]
  (str "http://" domain
       (if-not (= port 80)
         (str ":" port)) path))

(defn do-wait
  []
  (Thread/sleep 5000))

(defn do-wait-forever
  []
  @(promise))

(defn fetch-page
  [method path]
  (let [request {:method method
                 :url (expand-url path)}
        response (sync-http-request request)]
    (dosync
     (ref-set current-page response))))

(defn fetch-page-browser
  [method path]
  (to (expand-url path)))

(defn get-body
  []
  (-> @current-page :body channel-buffer->string))

(defmacro check-response
  [& body]
  `(and (not (fact ~@body))
        (throw (RuntimeException. "failed"))))

(defn log-response
  []
  (println "logging response")
  (log/info (get-body)))

(defn be-at-the-page
  [page-name]
  (let [path (get page-names page-name)]
    (fetch-page-browser :get path)))
