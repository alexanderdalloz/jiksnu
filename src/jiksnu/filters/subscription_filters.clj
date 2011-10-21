(ns jiksnu.filters.subscription-filters
  (:use (ciste [debug :only (spy)]
               [filters :only (deffilter)])
        jiksnu.actions.subscription-actions
        (jiksnu [session :only (current-user-id)]))
  (:require (jiksnu [model :as model]
                    [namespace :as namespace])
            (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.model [user :as model.user])))

(deffilter #'delete :http
  [action request]
  (let [{{id :id} :params} request]
    (action id)))

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'ostatus :http
  [action request]
  (action))

(deffilter #'ostatussub :http
  [action request]
  (let [{{profile :profile} :params} request]
    (action profile)))

(deffilter #'ostatussub-submit :http
  [action request]
  (let [{{profile :profile} :params} request]
    (if profile
      (let [[username password] (clojure.string/split profile #"@")
            user (actions.user/find-or-create username password)]
        (action user)))))

(deffilter #'subscribe :http
  [action request]
  (if-let [{{user-id :subscribeto} :params} request]
    (if-let [user (model.user/fetch-by-id user-id)]
      (action user))))

(deffilter #'subscribers :http
  [action request]
  (let [{{id :id} :params} request
        user (model.user/fetch-by-id id)]
    (action user)))

(deffilter #'subscriptions :http
  [action request]
  (let [{{id :id} :params} request
        user (model.user/fetch-by-id id)]
    (action user)))

(deffilter #'unsubscribe :http
  [action request]
  (if-let [actor (current-user-id)]
    (if-let [{{user :unsubscribeto} :params} request]
      (action actor (model/make-id user)))))






(deffilter #'remote-subscribe-confirm :xmpp
  [action request]
  (let [subscriber (actions.user/fetch-by-jid (:to request))
        subscribee (actions.user/fetch-by-jid (:from request))
        subscription (find-record
                      {:to (:_id subscribee) :from (:_id subscriber)})]
    (confirm subscription)))

(deffilter #'subscribe :xmpp
  [action request]
  (let [user (actions.user/fetch-by-jid (:to request))]
    (action user)))

(deffilter #'subscribed :xmpp
  [action request]
  (let [subscriber (actions.user/fetch-by-jid (:from request))
        subscribee (actions.user/fetch-by-jid (:to request))]
    (action subscriber subscribee)))

(deffilter #'subscribers :xmpp
  [action request]
  (if-let [user (actions.user/fetch-by-jid (:to request))]
    (action user)))

(deffilter #'subscriptions :xmpp
  [action request]
  (let [user (actions.user/fetch-by-jid (:to request))]
    (action user)))

(deffilter #'unsubscribe :xmpp
  [action request]
  (let [{:keys [to from]} request
        user (actions.user/fetch-by-jid to)
        subscriber (actions.user/find-or-create-by-jid from)]
    (action (:_id subscriber) (:_id user))))
