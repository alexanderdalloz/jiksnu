(ns jiksnu.xmpp.controller.subscription-controller
  (:use jiksnu.model
        jiksnu.namespace
        jiksnu.xmpp.view)
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]))

(defn subscriptions
  [request]
  (let [recipient (model.user/fetch-by-jid (:to request))
        subscriptions (model.subscription/subscriptions recipient)]
    subscriptions))

(defn subscribers
  [request]
  (let [recipient (model.user/fetch-by-jid (:to request))
        subscriptions (model.subscription/subscribers recipient)]
    subscriptions))

(defn subscribe
  [request]
  (let [to (:to request)
        from (:from request)]
    (let [user (model.user/fetch-by-jid to)
          subscriber (model.user/find-or-create-by-jid from)]
      (model.subscription/subscribe (:_id subscriber)
                                    (:_id user)))))

(defn remote-subscribe
  [actor user]
  ["subscribe" {"xmlns" pubsub-uri
                "node" microblog-uri
                "jid" (str (:username user) "@" (:domain user))}])

(defn remote-subscribe-confirm
  [request]
  true)

(defn subscribed
  [request]
  '())

(defn unsubscribe
  [request]
  (let [to (:to request)
        from (:from request)]
    (let [user (model.user/fetch-by-jid to)
          subscriber (model.user/find-or-create-by-jid from)]
      (model.subscription/unsubscribe (:_id subscriber)
                                      (:_id user))
      true)))
