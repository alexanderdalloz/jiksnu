(ns jiksnu.routes.domain-routes
  (:use [jiksnu.routes.helpers :only [add-route! named-path formatted-path]])
  (:require [jiksnu.actions.domain-actions :as domain]))

(add-route! "/.well-known/host-meta"     {:named "host meta"})
(add-route! "/main/domains/:id/discover" {:named "discover domain"})
(add-route! "/main/domains"              {:named "index domains"})
(add-route! "/main/domains/:id"          {:named "show domain"})
(add-route! "/model/domains/:id"         {:named "domain model"})

(defn routes
  []
  [
   [[:get    (formatted-path "host meta")]       #'domain/show]
   [[:get    (named-path     "host meta")]       {:action #'domain/show
                                                  :format :xrd}]
   [[:get    (formatted-path "index domains")]   #'domain/index]
   [[:get    (named-path     "index domains")]   #'domain/index]
   [[:get    (formatted-path "show domain")]     #'domain/show]
   [[:get    (named-path     "show domain")]     #'domain/show]
   [[:delete "/main/domains/*"]                  #'domain/delete]
   [[:post   (named-path     "discover domain")] #'domain/discover]
   [[:post   "/main/domains/:id/edit"]           #'domain/edit-page]
   [[:post   (named-path "index domains")]       #'domain/find-or-create]
   [[:get    (formatted-path "domain model")]    #'domain/show]
   ;; [[:get    "/api/dialback"]                    #'domain/dialback]
   ])

(defn pages
  []
  [
   [{:name "domains"}    {:action #'domain/index}]
   ])
