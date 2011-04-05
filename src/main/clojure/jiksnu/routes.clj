(ns jiksnu.routes
  (:use ciste.core
        ciste.predicates
        jiksnu.middleware
        jiksnu.namespace
        [ring.middleware.session :only (wrap-session)])
  (:require [ciste.middleware :as middleware]
            [compojure.core :as compojure]
            [compojure.route :as route]
            (jiksnu.actions
             [activity-actions :as activity]
             [auth-actions :as auth]
             [domain-actions :as domain]
             [inbox-actions :as inbox]
             [subscription-actions :as subscription]
             [user-actions :as user]
             [webfinger-actions :as webfinger])
            [jiksnu.view :as view]
            (jiksnu.views
             activity-views
             auth-views
             domain-views
             subscription-views
             user-views
             webfinger-views)))

(def #^:dynamic *standard-middleware*
  [#'wrap-log-request
   #'wrap-log-params])

;; (defn with-template
;;   [& forms]
;;   forms)

(compojure/defroutes all-routes
  (route/files "/public")
  (compojure/GET "/favicon.ico" request
       (route/files "favicon.ico"))
  (#'resolve-routes @*routes*)
  (route/not-found "/public/404.html"))

(def app
  (-> #'all-routes
      (wrap-user-debug-binding)
      (wrap-user-binding)
      (wrap-debug-binding)
      (wrap-database)
      ;; #'wrap-flash
      (wrap-session)
      (middleware/wrap-http-serialization)
      (wrap-error-catching)))

(def http-matchers
  (make-matchers
   [[:get "/"]                                       #'activity/index
    [:get "/main/register"]                          #'user/register
    [:get "/settings/profile"]                       #'user/profile
    [:get "/posts.:format"]                          #'activity/index
    [:get "/posts/new"]                              #'activity/new
    [:get "/notice/:id"]                             #'activity/show
    [:get "/notice/:id.:format"]                     #'activity/show
    [:get "/notice/:id/edit"]                        #'activity/edit
    [:get "/notice/:id/comment"]                     #'activity/new-comment
    [:post "/notice/:id/comments/update"]            #'activity/fetch-comments
    [:get "/api/statuses/user_timeline/:id.:format"] #'activity/user-timeline
    [:get "/api/statuses/public_timeline.:format"]   #'activity/index
    [:get "/admin/subscriptions"]                    #'subscription/index
    [:get "/admin/users"]                            #'user/index
    [:get "/main/login"]                             #'auth/login-page
    [:post "/main/guest-login"]                      #'auth/guest-login
    [:post "/main/login"]                            #'auth/login
    [:post "/main/logout"]                           #'auth/logout
    [:get "/main/password"]                          #'auth/password-page
    [:get  "/main/ostatus"]                          #'subscription/ostatus
    [:get "/main/ostatussub"]                        #'subscription/ostatussub
    [:post "/main/ostatussub"]                  #'subscription/ostatussub-submit
    [:post "/main/subscribe"]                        #'subscription/subscribe
    [:post "/main/unsubscribe"]                      #'subscription/unsubscribe
    [:delete "/subscriptions/:id"]                   #'subscription/delete
    [:post "/notice/new"]                            #'activity/create
    [:post "/notice/:id"]                            #'activity/update
    [:post "/notice/:id/likes"]                      #'activity/like-activity
    [:delete "/notice/:id"]                          #'activity/delete
    [:get "/domains"]                                #'domain/index
    [:get "/domains/*"]                              #'domain/show
    [:delete "/domains/*"]                           #'domain/delete
    [:post "/domains"]                               #'domain/create
    [:post "/domains/*/discover"]                    #'domain/discover
    [:post "/main/register"]                         #'user/create
    [:get "/users/:id"]                              #'user/remote-profile
    [:delete "/users/:id"]                           #'user/delete
    [:post "/:username"]                             #'user/update
    [:get "/:username/all"]                          #'inbox/index
    [:get "/:id"]                                    #'user/show
    [:get "/:id.:format"]                            #'user/show
    [:get "/:id/subscriptions"]                     #'subscription/subscriptions
    [:get "/:id/subscribers"]                        #'subscription/subscribers
    [:get "/.well-known/host-meta"]                  #'webfinger/host-meta
    [:get "/main/xrd"]                               #'webfinger/user-meta]))

(def xmpp-matchers
  (map
   (fn [[m a]]
     [(merge {:serialization :xmpp
              :format :xmpp} m) a])
   [[{:method :get
      :pubsub true
      :name "items"
      :node microblog-uri}
     #'activity/index]

    [{:method :set
      :pubsub true
      :name "publish"
      :node microblog-uri}
     #'activity/create]

    [{:method :get
      :pubsub true
      :name "items"
      :node (str microblog-uri ":replies:item=:id")}
     #'activity/fetch-comments]

    [{:method :get
      :pubsub true
      :node inbox-uri}
     #'inbox/index]

    [{:method :get
      :name "query"
      :ns query-uri}
     #'user/show]

    [{:method :set
      :name "publish"
      :ns vcard-uri}
     #'user/create]

    [{:method :result
      :name "query"
      :ns query-uri}
     #'user/remote-create]

    [{:method :get
      :name "subscriptions"}
     #'subscription/subscriptions]

    [{:method :set
      :name "subscribe"
      :ns pubsub-uri}
     #'subscription/subscribe]

    [{:method :set
      :name "unsubscribe"
      :ns pubsub-uri}
     #'subscription/unsubscribe]

    [{:method :get
      :name "subscribers"}
     #'subscription/subscribers]

    [{:method :set
      :name "subscribers"}
     #'subscription/subscribed]

    [{:method :result
      :name "subscription"
      :ns pubsub-uri}
     #'subscription/remote-subscribe-confirm]

    [{:method :headline}
     #'activity/remote-create]])
  )

(dosync
 (ref-set
  *routes*
  (concat
   http-matchers
   xmpp-matchers)))

(dosync
 (ref-set *matchers*
          [[#'xmpp-serialization?
            [#'type-matches?
             #'node-matches?
             #'name-matches?
             #'ns-matches?]]
           [#'http-serialization?
            [#'request-method-matches?
             #'path-matches?]]]))

