(ns jiksnu.filters.admin.group-filters
    (:use [ciste.filters :only [deffilter]]
          [jiksnu.actions.admin.group-actions :only [index]]))

(deffilter #'index :http
  [action request]
  (-> request :params
      (select-keys #{:page})
       action))