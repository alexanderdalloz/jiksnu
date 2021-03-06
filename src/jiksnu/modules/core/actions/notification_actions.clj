(ns jiksnu.modules.core.actions.notification-actions
  (:require [jiksnu.model.notification :as model.notification]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [slingshot.slingshot :refer [throw+]]))

(def model-ns 'jiksnu.model.notification)

(defn prepare-create
  [activity]
  (-> activity
      transforms/set-_id
      transforms/set-created-time
      transforms/set-updated-time))

(defn create
  "create an activity"
  [params]
  (let [item (prepare-create params)]
    (model.notification/create item)))

(def can-delete? (constantly true))

(def delete    (templates.actions/make-delete model.notification/delete can-delete?))

(def index* (templates.actions/make-indexer model-ns))

(defn index
  [& options]
  (apply index* options))

(defn show
  [notification]
  notification)

(defn fetch-by-user
  ([user]
   (fetch-by-user user nil))
  ([user options]
   (index {:owner (:_id user)} options)))
