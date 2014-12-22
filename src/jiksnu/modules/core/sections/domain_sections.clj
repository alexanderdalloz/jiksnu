(ns jiksnu.modules.core.sections.domain-sections
  (:require [ciste.core :refer [with-format]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section add-form delete-button index-block
                                            index-line show-section uri]]
            [clojure.tools.logging :as log]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.session :refer [current-user is-admin?]]
            [jiksnu.modules.core.sections :refer [admin-index-block admin-index-line]]
            [jiksnu.modules.web.sections :refer [action-link bind-to display-property
                                                 dropdown-menu]]
            [jiksnu.namespace :as ns]
            [jiksnu.session :as session])
  (:import jiksnu.model.Domain))

;; admin-index-block

(defsection admin-index-block [Domain :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] {(:_id m) (admin-index-line m page)}))
       (into {})))

(defsection index-block [Domain :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

(defsection show-section [Domain :jrd]
  [item & [page]]
  (let [id (:_id item)
        template (format "http://%s/main/xrd?uri={uri}" id)]
    {:host id
     :links [{:template template
              :rel "lrdd"
              :title "Resource Descriptor"}]}))

(defsection show-section [Domain :json]
  [item & [page]]
  item)

(defsection show-section [Domain :model]
  [item & [page]]
  item)

(defsection show-section [Domain :viewmodel]
  [item & [page]]
  item)

(defsection show-section [Domain :xrd]
  [item & [page]]
  (let [id (:_id item)]
    ["XRD" {"xmlns" ns/xrd
            "xmlns:hm" ns/host-meta}
     ["hm:Host" id]
     ["Subject" id]
     (->> (:links item)
          (map
           ;; TODO: show-section [Link :xrd]
           (fn [{:keys [title rel href template] :as link}]
             [:Link (merge {}
                           (if rel {:rel rel})
                           (if href {:href href})
                           (if template {:template template}))
              (if title
                [:Title title])])))]))

;; uri

(defsection uri [Domain]
  [domain & _]
  (str "/main/domains/" (:_id domain)))
