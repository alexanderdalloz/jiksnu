(ns jiksnu.model.feed-subscription-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [check context test-environment-fixture]]
        [jiksnu.model.feed-subscription :only [create count-records delete drop!
                                               fetch-all fetch-by-id]]
        [midje.sweet :only [throws =>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.util :as util])
  (:import jiksnu.model.FeedSubscription
           org.bson.types.ObjectId
           org.joda.time.DateTime
           slingshot.ExceptionInfo))

(test-environment-fixture

 (context #'count-records
   (context "when there aren't any items"
     (drop!)
     (count-records) => 0)
   (context "when there are items"
     (drop!)
     (let [n 15]
       (dotimes [i n]
         (mock/a-feed-subscription-exists))
       (count-records) => n)))

 (context #'delete
   (let [item (mock/a-feed-subscription-exists)]
     (delete item) => item
     (fetch-by-id (:_id item)) => nil))

 (context #'drop!
   (dotimes [i 1]
     (mock/a-feed-subscription-exists))
   (drop!)
   (count-records) => 0)

 (context #'fetch-by-id
   (context "when the item doesn't exist"
     (let [id (util/make-id)]
       (fetch-by-id id) => nil?))

   (context "when the item exists"
     (let [item (mock/a-feed-subscription-exists)]
       (fetch-by-id (:_id item)) => item)))

 (context #'create
   (context "when given valid params"
     (let [params (actions.feed-subscription/prepare-create
                   (factory :feed-subscription {:local false}))]
       (create params)) =>
       (check [response]
         response => map?
         response => (partial instance? FeedSubscription)
         (:_id response) =>  (partial instance? ObjectId)
         (:created response) => (partial instance? DateTime)
         (:updated response) => (partial instance? DateTime)
         (:url response) => string?))

   (context "when given invalid params"
     (create {}) => (throws RuntimeException)))

 (context #'fetch-all
   (context "when there are no items"
     (drop!)
     (fetch-all) => empty?)

   (context "when there is more than a page of items"
     (drop!)

     (let [n 25]
       (dotimes [i n]
         (mock/a-feed-subscription-exists))

       (fetch-all) =>
       (check [response]
         response => seq?
        (count response) => 20)

       (fetch-all {} {:page 2}) =>
       (check [response]
         response => seq?
         (count response) => (- n 20)))))

 )

