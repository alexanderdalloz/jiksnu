(ns jiksnu.modules.web.routes.home-routes
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as activity]
            [jiksnu.actions.site-actions :as site]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers :as helpers
             :refer [angular-resource]]
            [octohipster.mixins :as mixin
             :refer [item-resource]]))

(defgroup root
  :url ""
  :summary "Root")

(defresource root home
  :url "/"
  :summary "home page"
  :mixins [angular-resource]
  :doc {:get {:nickname "home-page"
              :summary "Home Page"}})

(defresource root status
  :url "/status"
  :summary "Site Status"
  :description "Contains base data used to initialize the front-end application"
  :mixins [item-resource]
  :exists? (fn [ctx] {:data (site/status)}))