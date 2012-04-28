(ns jiksnu.actions.inbox-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]))
  (:require (jiksnu.model [item :as model.item])))

(defaction index
  [user]
  (model.item/fetch-activities user))

(definitializer
  (doseq [namespace ['jiksnu.filters.inbox-filters
                     'jiksnu.views.inbox-views]]
    (require namespace)))
