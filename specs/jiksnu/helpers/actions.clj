(ns jiksnu.helpers.actions
  (:require [clj-http.client :as client]
            [taoensso.timbre :as timbre]
            [manifold.stream :as s]
            [manifold.time :as time]
            [jiksnu.specs.protocols :as lp]
            [midje.sweet :refer :all]
            [slingshot.slingshot :refer [throw+ try+]]
            [clj-webdriver.driver :as driver]
            [clj-webdriver.taxi :as taxi])
  (:import (org.openqa.selenium.remote DesiredCapabilities CapabilityType RemoteWebDriver)
           (org.openqa.selenium Platform)
           (java.net URL)))

(def default-sleep-time (time/seconds 5))

(def page-names
  {"home"                           "/"
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
(def that-stream (s/stream* {:permanent? true}))

(defn get-domain
  []
  domain)

(defn get-host
  []
  (str domain
       (if-not (= port 80)
         (str ":" port))))

(defn expand-url
  [path]
  (str "https://" (get-host) path))

(defn do-wait
  []
  (Thread/sleep default-sleep-time))

(defn do-wait-forever
  []
  @(promise))

(defn fetch-page
  [_method path]
  ;; TODO: Handle non-GET case
  (let [response (client/get (expand-url path))]
    (dosync
     (ref-set current-page response))))

;(defn fetch-page-browser
;  [method path]
;  (to (expand-url path)))

(defn get-body
  []
  (-> @current-page :body))

(defmacro check-response
  [& body]
  `(try+ (and (not (fact ~@body))
              (throw+ "failed"))
         (catch RuntimeException ex#
           (.printStackTrace ex#)
           (throw+ ex#))))

(defn log-response
  []
  (timbre/info (get-body)))

;; (defn be-at-the-page
;;  [page-name]
;;  (let [path (get page-names page-name)]
;;    (fetch-page-browser :get path)))

(def server (atom nil))
(def driver (atom nil))

(def selenium-config
  {:host "selenium"
   :port 24444})

(defn restart-session
  []
  (when (not @driver)
    (let [{:keys [host port]} selenium-config
          caps (doto (DesiredCapabilities.)
                 (.setCapability CapabilityType/BROWSER_NAME "firefox")
                 ;(.setCapability CapabilityType/PLATFORM Platform/MAC)
                 (.setCapability "name" "clj-webdriver-test-suite"))
          url (str "http://" host ":" port "/wd/hub")
          wd (RemoteWebDriver. (URL. url) caps)
          session-id (str (.getSessionId wd))]
      (timbre/infof "Session Id: %s" session-id)
      (reset! driver wd))))


(defn register-user
  [password]
  (timbre/info "registering user")
  (restart-session)
  ;; (let [[a-server a-driver] (taxi/new-remote-session {:port 4444
  ;;                                                     :host "selenium"}
  ;;                                                    {:browser :firefox})]
  ;;   (taxi/set-)
  ;;   (taxi/set-driver! a-driver))

  (let [d (driver/init-driver @driver)]
    (timbre/infof "driver: %s" (driver/driver? d))
    (taxi/set-driver! d)
    (taxi/to "https://www.google.com/" #_(expand-url "/"))
    nil))

(defn login-user
  "Log in with test user"
  []
  #_(let [page (LoginPage.)]
    (timbre/info "Fetching login Page")
    (lp/load-page page)

    (timbre/info "Logging in")
    (-> (lp/login page "test" "test")
        (.then (fn [] (timbre/info "login finished"))))))
