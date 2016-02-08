(ns jiksnu.modules.core.views.client-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section show-section]]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.util :as util]))

(defview #'actions.client/index :page
  [request response]
  (taoensso.timbre/info "applying client index view")
  (util/inspect response)
  {:body (merge
           response
           {:name (:name request)})})
