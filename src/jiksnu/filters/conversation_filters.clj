(ns jiksnu.filters.conversation-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.conversation-actions)
  (:require [jiksnu.model.conversation :as model.conversation]))

(deffilter #'create :http
  [action request]
  (-> request :params action))

(deffilter #'delete :http
  [action request]
  (-> request :params :id model.conversation/fetch-by-id action))

(deffilter #'index :http
  [action request]
  (-> request :params action))

(deffilter #'show :http
  [action request]
  (-> request :params :id model.conversation/fetch-by-id action))
