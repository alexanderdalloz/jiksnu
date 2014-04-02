(ns jiksnu.modules.core.views.activity-views
  (:require [ciste.config :refer [config]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section show-section]]
            [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [jiksnu.modules.web.sections :refer [bind-to]]
            [jiksnu.modules.xmpp.element :as xmpp.element]
            [ring.util.response :as response])
  (:import jiksnu.model.Activity))

(defview #'actions.activity/delete :model
  [request activity]
  {:body (show-section activity)})

;; fetch-by-conversations

(defview #'actions.activity/fetch-by-conversation :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "sub-page-updated"
            :model "conversation"
            :id (:_id (:item request))
            :body response}}))

;; index

(defview #'actions.activity/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

;; oembed

(defview #'actions.activity/oembed :json
  [request oembed-map]
  {:status 200
   :body oembed-map})

(defview #'actions.activity/oembed :xml
  [request m]
  {:status 200
   :body
   [:oembed
    [:version (:version m)]
    [:type (:type m)]
    [:provider_name (:provider_name m)]
    [:provider_url (:provider_url m)]
    [:title (:title m)]
    [:author_name (:author_name m)]
    [:author_url (:author_url m)]
    [:url (:url m)]
    [:html (:html m)]]})

;; show

(defview #'actions.activity/show :clj
  [request activity]
  {:body activity})

(defview #'actions.activity/show :json
  [request activity]
  {:body (show-section activity)})

(defview #'actions.activity/show :model
  [request activity]
  {:body (show-section activity)})

(defview #'actions.activity/show :viewmodel
  [request activity]
  {:body {:activities (doall (index-section [activity]))
          :targetActivity (:_id activity)
          :title (:title activity)}})
