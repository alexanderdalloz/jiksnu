(ns jiksnu.routes
  (:use aleph.http
        ciste.debug
        ciste.filters
        ciste.predicates
        ciste.routes
        jiksnu.middleware
        jiksnu.namespace
        lamina.core
        (ring.middleware params
                         keyword-params
                         flash
                         nested-params
                         multipart-params
                         cookies
                         session))
  (:require [ciste.middleware :as middleware]
            [compojure.core :as compojure]
            compojure.handler
            [clojure.string :as string]
            [clojure.java.io :as io]
            [compojure.route :as route]
            (jiksnu.actions
             [activity-actions :as activity]
             [auth-actions :as auth]
             [domain-actions :as domain]
             [inbox-actions :as inbox]
             [push-subscription-actions :as push]
             [salmon-actions :as salmon]
             [settings-actions :as settings]
             [subscription-actions :as subscription]
             [user-actions :as user]
             [webfinger-actions :as webfinger])
            [jiksnu.view :as view]
            (jiksnu.filters
             activity-filters
             auth-filters
             domain-filters
             push-subscription-filters
             salmon-filters
             subscription-filters
             user-filters)
            (jiksnu.views
             activity-views
             auth-views
             domain-views
             push-subscription-views
             subscription-views
             user-views
             webfinger-views)
            (jiksnu.triggers
             activity-triggers
             domain-triggers
             subscription-triggers
             user-triggers)
            [ring.util.response :as response]))

(defn not-found-msg
  []
  "You found the hidden page. Don't tell anyone about this.\n")

(defn escape-route
  [path]
  (string/replace path #":" "\\:"))

(def http-routes
  (make-matchers
   [[[:get  "/"]     #'activity/index]
    [[:get "/.well-known/host-meta"]       #'webfinger/host-meta]
    [[:get "/admin/subscriptions"]         #'subscription/index]
    [[:get "/admin/push/subscriptions"]    #'push/index]
    [[:get "/admin/users"]                 #'user/index]
    [[:get "/admin/settings"]              #'settings/edit]
    [[:get "/api/people/@me/@all"]         #'user/index]
    [[:get "/api/people/@me/@all/:id"]     #'user/show]
    [[:get "/api/statuses/public_timeline.:format"]    #'activity/index]
    [[:get  "/api/statuses/user_timeline/:id.:format"] #'activity/user-timeline]
    [[:get "/domains"]                     #'domain/index]
    [[:get "/domains/*"]                   #'domain/show]
    [[:delete "/domains/*"]                #'domain/delete]
    [[:post "/domains"]                    #'domain/create]
    [[:post "/domains/*/discover"]         #'domain/discover]
    [[:post "/domains/*/edit"]             #'domain/edit]
    [[:post "/main/guest-login"]           #'auth/guest-login]
    [[:get "/main/login"]                  #'auth/login-page]
    [[:post "/main/login"]                 #'auth/login]
    [[:post "/main/logout"]                #'auth/logout]
    ;; [[:get "/main/events"]                 #'activity/stream]
    [[:get "/main/ostatus"]                #'subscription/ostatus]
    [[:get "/main/ostatussub"]             #'subscription/ostatussub]
    [[:post "/main/ostatussub"]            #'subscription/ostatussub-submit]
    [[:get "/main/password"]               #'auth/password-page]
    [[:get "/main/push/hub"]               #'push/hub]
    [[:post "/main/push/hub"]              #'push/hub-publish]
    [[:get "/main/push/callback"]          #'push/callback]
    [[:post "/main/push/callback"]         #'push/callback-publish]
    [[:get "/main/register"]               #'user/register]
    [[:post "/main/register"]              #'user/create]
    [[:post "/main/salmon/user/:id"]       #'salmon/process]
    [[:post "/main/subscribe"]             #'subscription/subscribe]
    [[:post "/main/unsubscribe"]           #'subscription/unsubscribe]
    [[:get "/main/xrd"]                    #'webfinger/user-meta]
    [[:get "/notice/:id"]                  #'activity/show]
    [[:get "/notice/:id.:format"]          #'activity/show]
    [[:get  "/notice/:id/comment"]         #'activity/new-comment]
    [[:post "/notice/:id/comments/update"] #'activity/fetch-comments]
    [[:get "/notice/:id/edit"]             #'activity/edit]
    [[:post "/notice/:id/likes"]           #'activity/like-activity]
    [[:post "/notice/new"]                 #'activity/post]
    [[:post "/notice/:id"]                 #'activity/update]
    [[:delete "/notice/:id"]               #'activity/delete]
    [[:get "/remote-user/*"]               #'user/remote-user]
    [[:get "/settings/profile"]            #'user/profile]
    [[:delete "/subscriptions/:id"]        #'subscription/delete]
    [[:delete "/users/:id"]                #'user/delete]
    [[:get "/users/:id"]                   #'user/remote-profile]
    [[:post "/users/:id/discover"]         #'user/discover]
    [[:post "/users/:id/update"]           #'user/fetch-updates]
    [[:post "/users/:id/update-hub"]       #'user/update-hub]
    [[:post "/users/:id/push/subscribe"]   #'push/subscribe]
    [[:get "/:id"]                         #'user/show]
    [[:post "/:username"]                  #'user/update]
    [[:get "/:id.:format"]                 #'user/show]
    [[:get "/:username/all"]               #'inbox/index]
    [[:get "/:id/subscribers"]             #'subscription/subscribers]
    [[:get "/:id/subscriptions"]           #'subscription/subscriptions]]))

(def xmpp-routes
  (map
   (fn [[m a]]
     [(merge {:serialization :xmpp
              :format :xmpp} m)
      {:action a}])
   [[{:method :get
      :pubsub true
      :name "items"
      :node (escape-route microblog-uri)}
     #'activity/user-timeline]

    [{:method :set
      :pubsub true
      :name "publish"
      :node (escape-route microblog-uri)}
     #'activity/post]

    [{:method :get
      :pubsub true
      :name "items"
      :node (str microblog-uri ":replies:item=:id")}
     #'activity/fetch-comments]

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

    [{:method :result
      :pubsub true
      :node (str microblog-uri ":replies:item=:id")
      :ns pubsub-uri}
     #'activity/comment-response]

    [{:method :get
      :name "subscriptions"}
     #'subscription/subscriptions]

    [{:method :set
      :name "subscribe"
      :ns pubsub-uri}
     #'subscription/subscribed]

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

    ;; FIXME: This is way too general
    [{:method :headline}
     #'activity/remote-create]

    [{:method :result
      :pubsub true
      :node microblog-uri}
     #'activity/remote-create]

    [{:method :get
      :pubsub true
      :node (escape-route inbox-uri)}
     #'inbox/index]

    [{:method :result
      :pubsub false} #'domain/ping-response]]))

(def #^:dynamic *standard-middleware*
  [#'wrap-log-request
   #'wrap-log-params])

(def http-predicates
  [#'http-serialization?
   [#'request-method-matches?
    #'path-matches?]])

(def xmpp-predicates
  [#'xmpp-serialization?
   [#'type-matches?
    #'node-matches?
    #'name-matches?
    #'ns-matches?]])

(compojure/defroutes all-routes
  (route/files "/public")
  (resolve-routes [http-predicates] http-routes)
  (compojure/GET "/main/events" _
                 (wrap-aleph-handler activity/stream-handler))
  (compojure/GET "/favicon.ico" request
                 (response/file-response "favicon.ico"))
  (compojure/GET "/robots.txt" _
                 (response/file-response "public/robots.txt"))
  (route/not-found (not-found-msg)))

(def app
  (wrap-ring-handler
   (-> all-routes
       wrap-log-request
       wrap-flash
       ;; wrap-nested-params
       wrap-multipart-params
       wrap-log-request
       wrap-keyword-params
       wrap-params
       wrap-user-binding
       middleware/wrap-http-serialization
       wrap-flash
       wrap-database
       wrap-session
       wrap-error-catching)))

#_(def app
  (wrap-ring-handler
   (wrap-multipart-params
    (compojure/POST "/notice/new" [file]
                    (io/copy (:picture file) (io/file "picture.jpg"))))

   (route/files "/public")))
