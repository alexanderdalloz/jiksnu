(ns jiksnu.modules.admin.views.user-views
  (:use [ciste.config :only [config]]
        [ciste.sections.default :only [title]]
        [ciste.views :only [defview]]
        [jiksnu.modules.admin.actions.user-actions :only [index show]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.core.sections :only [admin-index-section admin-index-block
                                             admin-show-section]]
        [jiksnu.modules.web.sections :only [bind-to with-page
                                            with-sub-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.modules.web.sections.user-sections :as sections.user])
  (:import jiksnu.model.Activity
           jiksnu.model.Stream))

(defview #'index :html
  [request {:keys [items] :as page}]
  {:single true
   :title "Users"
   :body (admin-index-section items page)})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:single true
   :body
   {:title "Users"
    :site {:name (config :site :name)}
    :showPostForm false
    :notifications []}})

;; show

(defview #'show :html
  [request user]
  {:title (title user)
   :single true
   :body
   [:div {:data-model "user"
          :data-id "{{user.id}}"}
    (admin-show-section user)
    (let [links [{}]]
      (sections.user/links-table links))
    (with-sub-page "activities"
      [:h3 "Activities"]
      (let [page {}
            items [(Activity.)]]
        (admin-index-section items page)))
    (with-sub-page "streams"
      [:h3 "Streams"]
      (let [page {}
            items [(Stream.)]]
        (admin-index-section items page)))
    [:add-stream-form]]})

(defview #'show :model
  [request user]
  {:body (admin-show-section user) })

(defview #'show :viewmodel
  [request user]
  {:body
   {:title (title user)
    :targetUser (:_id user)}})

