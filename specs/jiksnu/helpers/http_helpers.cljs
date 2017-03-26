(ns jiksnu.helpers.http-helpers
  (:require [cljs.nodejs :as nodejs]
            [clojure.string :as string]
            [jiksnu.helpers.page-helpers :refer [base-domain base-path]]
            [taoensso.timbre :as timbre]))

(def child-process (nodejs/require "child_process"))
(def JSData (nodejs/require "js-data"))
(def DSHttpAdapter (nodejs/require "js-data-http"))
(def adapter (DSHttpAdapter.))
(def store (JSData.DS))

(.registerAdapter store "http" adapter #js {:default true})
;; (def HttpAdapter (.-HttpAdapter (nodejs/require "js-data-http-node")))
(timbre/infof "Base Path: %s" base-path)
;; (def http-adapter (HttpAdapter. #js {:basePath base-path}))

(defn get-cookie-map
  "Returns the cookie data from a response map"
  [response]
  (if-let [set-cookie-string (first (aget response.headers "set-cookie"))]
    (->> (string/split set-cookie-string #";")
         (map (fn [s] (let [[k v] (string/split s #"=")] [k v])))
         (into {}))
    (throw "No set cookie header sent")))

(defn authenticate
  "Authenticate the test user. Get a cookie."
  ([] (authenticate nil))
  ([cookie]
   (let [d (.defer js/protractor.promise)
         data #js {:username "test"
                   :password "test"}]
     #_(.fulfill d true)
     ;; js/debugger
     #_
     (.. http-adapter
         (GET "/main/login")
         (then (fn [data]
                 (js/console.log "data" data)
                 (if (#{200 303} (.-status data))
                   (.fulfill d data)
                   (.reject d data)))))
     d.promise)))

(defn get-fortune
  []
  (let [d (js/protractor.promise.defer)]
    (.exec child-process "/usr/games/fortune" #js {}
           (fn [err stdout stderr]
             (if err
               (.reject d)
               (.fulfill d (string/replace stdout #"\n" "\n\n")))))
    d.promise))

(defn an-activity-exists
  "Create a mock activity"
  []
  (let [d (.defer js/protractor.promise)]
    (-> (get-fortune)
        (.then (fn [text]
                (timbre/infof "Text: %s" text)
                 (let [activity #js {:content text}
                       url (str base-path "/model/activities")
                       data #js {:auth #js {:username "test" :password "test"}}]
                   #_
                   (.POST http-adapter url activity data))))
        (.then (fn [response]
                 (let [status-code (.-status response)]
                   (timbre/debugf "Status Code: %s" status-code)
                   (if (#{200 201} status-code)
                     (d.fulfill response)
                     (d.reject response))))))
    d.promise))

(defn user-exists?
  "Queries the server to see if a user exists with that name"
  [username]
  (let [d (js/protractor.promise.defer)
        url (str "/api/user/" username)]
    #_
    (.. http-adapter
        (GET url)
        (then (fn [response]
                (if (= (.-statusCode response) 200)
                  (.fulfill d true)
                  (.reject d #js {:response response})))))
    (.-promise d)))
