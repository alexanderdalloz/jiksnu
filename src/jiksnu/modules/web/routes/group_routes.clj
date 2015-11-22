(ns jiksnu.modules.web.routes.group-routes
  (:require [clojure.data.json :as json]
            [jiksnu.actions.conversation-actions :as conversation]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.model.group :as model.group]
            jiksnu.modules.core.views.group-views
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource path]]
            [jiksnu.util :as util]
            [octohipster.mixins :as mixin]
            [puget.printer :as puget]
            [taoensso.timbre :as timbre])
  (:import jiksnu.model.Group))

(defparameter :model.group/id
  :description "The Id of a conversation"
  :type "string")

;; =============================================================================

(defgroup jiksnu groups
  :url "/main/groups"
  :name "Groups")

(defresource groups :collection
  :mixins [angular-resource])

(defresource groups :item
  :url "/{_id}"
  :parameters {:_id (path :model.group/id)}
  :mixins [angular-resource])

;; (defresource groups resource

;;   )

;; =============================================================================

(defgroup jiksnu groups-api
  :name "Groups API"
  :url "/model/groups")

(defresource groups-api :collection
  :mixins [page-resource]
  :allowed-methods [:get :post]
  :new? :data
  :post-redirect? (fn [ctx] {:location (format "/model/groups/%s" (:data ctx))})
  :schema {:type "object"
           :properties {:name {:type "string"}}
           :required [:name]}
  :post! (fn [ctx]
           (timbre/info "Post to group")
           (let [params (:params (:request ctx))
                 group (actions.group/create params)]
             {:data (:_id group)}))
  :available-formats [:json]
  :ns 'jiksnu.actions.group-actions)

(defresource groups-api :item
  :desc "Resource routes for single Group"
  :url "/{_id}"
  :parameters {:_id (path :model.group/id)}
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :delete! (fn [ctx]
             (timbre/info "Delete group")
             (actions.group/delete (:data ctx)))
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)]
               {:data (model.group/fetch-by-id id)})))
