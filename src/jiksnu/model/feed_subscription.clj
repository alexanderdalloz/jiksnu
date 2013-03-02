(ns jiksnu.model.feed-subscription
  (:use [jiksnu.validators :only [type-of]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [acceptance-of presence-of valid? validation-set]])
  (:require [clj-statsd :as s]
            [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates :as templates]
            [lamina.trace :as trace]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.query :as mq]
            [monger.result :as result])
  (:import jiksnu.model.FeedSubscription
           org.bson.types.ObjectId
           org.joda.time.DateTime))

(def collection-name "feed_subscriptions")

(def create-validators
  (validation-set
   (type-of :_id      ObjectId)
   (type-of :url      String)
   (type-of :callback String)
   (type-of :domain   String)
   (type-of :local    Boolean)
   (type-of :created  DateTime)
   (type-of :updated  DateTime)))

(def set-field!    (templates/make-set-field! collection-name))

(defn fetch-by-id
  [id]
  (if-let [item (mc/find-map-by-id collection-name id)]
    (model/map->FeedSubscription item)))

(defn create
  [params]
  (let [errors (create-validators params)]
    (if (empty? errors)
      (do
        (log/debugf "Creating feed subscription: %s" params)
        (mc/insert collection-name params)
        (let [item (fetch-by-id (:_id params))]
          (trace/trace :feed-subscriptions:created item)
          (s/increment "feed-subscriptions_created")
          item))
      (throw+ {:type :validation
               :errors errors}))))

(def count-records (templates/make-counter collection-name))
(def delete        (templates/make-deleter collection-name))
(def drop!         (templates/make-dropper collection-name))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     ((templates/make-fetch-fn model/map->FeedSubscription collection-name)
      params options)))

(defn fetch-by-topic
  "Fetch a single source by it's topic id"
  [topic]
  (fetch-all {:topic topic}))

(defn ensure-indexes
  []
  (doto collection-name
    (mc/ensure-index {:url 1 :callback 1} {:unique true})))
