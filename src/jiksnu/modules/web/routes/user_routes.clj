(ns jiksnu.modules.web.routes.user-routes
  (:require [ciste.initializer :only [definitializer]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.group-actions :as group]
            [jiksnu.actions.stream-actions :as stream]
            [jiksnu.actions.subscription-actions :as sub]
            [jiksnu.actions.user-actions :as user]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.routes :as r]
            [octohipster.mixins :as mixin])
  (:import jiksnu.model.User))

(defresource users collection
  :desc "Collection route for users"
  :mixins [mixin/collection-resource]
  :exists? #'user/index)

(defgroup users
  :url "/users"
  ;; :resources [user-collection]
  )

(defn routes
  []
  [
   ;; [[:get    "/api/friendships/exists.:format"] #'user/exists?]

   ;; [[:get    "/api/people/@me/@all"]            #'user/index]

   ;; [[:get    "/api/people/@me/@all/:id"]        #'user/show]
   ;; [[:get    "/main/profile"]                   #'user/profile]
   ;; [[:get    "/main/register"]                  #'user/register-page]
   ;; [[:get    "/main/xrd"]                       #'user/user-meta]
   ;; [[:get    "/model/users/:id"]                #'user/show]
   ;; [[:get    "/users"]                          #'user/index]
   ;; [[:post   "/users/:id/update-hub"]           #'user/update-hub]
   ;; [[:post   "/:username"]                      #'user/update]


   [[:get    "/api/user/:username/"]            {:action #'user/show-basic
                                                 :format :as}]
   [[:get    "/api/user/:username/profile"]     {:action #'user/show
                                                 :format :as}]

   [[:post   "/main/profile"]                   #'user/update-profile]
   [[:post   "/main/register"]                  #'user/register]
   [[:get    "/users.:format"]                  #'user/index]
   [[:get    "/users/:id"]                      #'user/show]
   [[:get    "/users/:id.:format"]              #'user/show]
   [[:get    "/users/:user@:domain.:format"]    #'user/show]
   [[:delete "/users/:id"]                      #'user/delete]
   [[:post   "/users/:id/discover.:format"]     #'user/discover]
   [[:post   "/users/:id/discover"]             #'user/discover]
   [[:post   "/users/:id/update.:format"]       #'user/update]
   [[:post   "/users/:id/update"]               #'user/update]
   [[:post   "/users/:id/streams"]              #'user/add-stream]
   [[:post   "/users/:id/delete"]               #'user/delete]
   ])

(defn pages
  []
  [
   [{:name "users"}         {:action #'user/index}]
   ])

(defn sub-pages
  []
  [
   [{:type User :name "activities"}       {:action #'stream/user-timeline}]
   [{:type User :name "subscriptions"}    {:action #'sub/get-subscriptions}]
   [{:type User :name "subscribers"}      {:action #'sub/get-subscribers}]
   [{:type User :name "streams"}          {:action #'stream/fetch-by-user}]
   [{:type User :name "groups"}           {:action #'group/fetch-by-user}]

   ])
