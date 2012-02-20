(ns jiksnu.actions.group-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]))
  (:require (jiksnu.model [group :as model.group]))
  (:import jiksnu.model.Group))

(defaction new-page
  []
  (Group.))

(defaction index
  []
  (model.group/index))

(defaction user-list
  [user]
  ;; TODO: implement
  [])

(defaction add
  [params]
  (model.group/create params))

(definitializer
  (doseq [namespace ['jiksnu.filters.group-filters
                     ;; 'jiksnu.helpers.group-helpers
                     'jiksnu.views.group-views]]
    (require namespace)))
