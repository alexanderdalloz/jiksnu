(ns jiksnu.actions.admin.subscription-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.model :only [implement]]
        [ciste.runner :only [require-namespaces]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.session :as session])
  (:import javax.security.sasl.AuthenticationException))

(defaction create
  [params & options]
  (implement))

(defaction show
  [subscription]
  subscription)

(defaction delete
  [subscription]
  (actions.subscription/delete subscription))

(defaction update
  [subscription]
  (actions.subscription/update subscription))

;; requires admin
(defaction index
  [options]
  [(model.subscription/fetch-all) options])

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.subscription-filters"
    "jiksnu.views.admin.subscription-views"]))
