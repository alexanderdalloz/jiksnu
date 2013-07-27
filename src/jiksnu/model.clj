(ns jiksnu.model
  (:use [ciste.config :only [config]]
        [ciste.initializer :only [definitializer]]
        [clojurewerkz.route-one.core :only [*base-url*]])
  (:require [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.db :as db]
            [monger.core :as mg])
  (:import com.mongodb.WriteConcern))

;; TODO: pull these from ns/
(defonce bound-ns {:hm "http://host-meta.net/xrd/1.0"
                   :xrd "http://docs.oasis-open.org/ns/xri/xrd-1.0"})

(defrecord Activity                [])
(defrecord AuthenticationMechanism [])
(defrecord Conversation            [])
(defrecord Dialback                [])
(defrecord Domain                  [])
(defrecord FeedSource              [])
(defrecord FeedSubscription        [])
(defrecord Group                   [])
(defrecord GroupMembership         [])
(defrecord Item                    [])
(defrecord Key                     [])
(defrecord Like                    [])
(defrecord Resource                [])
(defrecord Stream                  [])
(defrecord Subscription            [])
(defrecord User                    [])

(def entity-names
  [
   Activity
   AuthenticationMechanism
   Conversation
   Dialback
   Domain
   FeedSource
   FeedSubscription
   Group
   GroupMembership
   Item
   Key
   Like
   Resource
   Stream
   Subscription
   User
   ]
  )

(def model-names
  [
   "activity"
   "authentication-mechanism"
   "conversation"
   "dialback"
   "domain"
   "feed-source"
   "feed-subscription"
   "group"
   "group-membership"
   "item"
   "key"
   "like"
   "resource"
   "subscription"
   "user"
   ]
  )

(def action-group-names
  ["activity"
   "auth"
   "comment"
   "confirm"
   "conversation"
   "domain"
   "favorite"
   "feed-source"
   "feed-subscription"
   "group"
   "like"
   "key"
   "message"
   "pubsub"
   "resource"
   "salmon"
   "search"
   "setting"
   "site"
   "stream"
   "subscription"
   "tag"
   "user"])



;; Entity predicates

(defn domain?
  [domain]
  (instance? Domain domain))

(defn subscription?
  [subscription]
  (instance? Subscription subscription))

(defn user?
  "Is the provided object a user?"
  [user] (instance? User user))

;; initializer

(definitializer
  (let [url (format "http://%s" (config :domain))]
    (alter-var-root #'*base-url*
                    (constantly url)))

  (s/setup "localhost" 8125)

  (db/set-database!)

  (mg/set-default-write-concern! WriteConcern/FSYNC_SAFE)
  )

