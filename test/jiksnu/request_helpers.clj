(ns jiksnu.request-helpers
  (:require [clj-http.client :as client]
            [clojure.string :as string]
            [lamina.core :as l]
            [jiksnu.action-helpers :refer [expand-url fetch-page fetch-page-browser page-names
                                           that-stream]]
            [jiksnu.model.user :as model.user]
            [jiksnu.referrant :refer [get-this get-that]]
            [ring.mock.request :as req]))

(defn request-oembed-resource
  []
  (fetch-page-browser :get (str "/main/oembed?format=json&url=" (:url (get-this :activity)))))

(defn request-stream
  [stream-name]
  ;; TODO: FIXME
  (let [ch (:body @(client/get (expand-url (page-names stream-name))))]
    (l/siphon ch that-stream)
    (Thread/sleep 3000)))

(defn request-page-for-user
  ([page-name] (request-page-for-user page-name nil))
  ([page-name format]
     (condp = page-name
       "subscriptions"
       (fetch-page :get
                   (str "/users/" (:_id (get-this :user)) "/subscriptions"
                        (when format
                          (str "." (string/lower-case format)))))
       "user-meta"
       (fetch-page :get
                   (str "/main/xrd?uri=" (model.user/get-uri (get-this :user)))))))

(defn request-user-meta
  []
  (fetch-page :get
              (str "/main/xrd?uri=" (model.user/get-uri (get-this :user)))))

