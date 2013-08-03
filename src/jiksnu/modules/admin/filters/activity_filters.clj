(ns jiksnu.modules.admin.filters.activity-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.admin.activity-actions :only [index]]
        [jiksnu.modules.core.filters :only [parse-page parse-sorting]])
  (:require [clojure.tools.logging :as log]))

;; index

(deffilter #'index :http
  [action request]
  (action {}
          (merge {}
                 (parse-page request)
                 (parse-sorting request))))
