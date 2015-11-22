(ns jiksnu.providers
  (:require jiksnu.app
            [taoensso.timbre :as timbre])
  (:use-macros [gyr.core :only [def.provider]]
               [purnam.core :only [? ?> ! !> obj arr]]))

(defn connect
  [app]
  (.send app "connect"))

(defn ping
  [app]
  (.send app "ping"))

(defn fetch-status
  [app]
  (timbre/debug "fetching app status")
  (let [$http (.. app -di -$http)
        path "/status"]
   (-> (.get $http path)
       (.then (fn [response]
                (timbre/debug "setting app status")
                (set! (.-data app) (.-data response)))))))

(defn login
  [app username password]
  (timbre/info "Logging in user." username password)
  (-> (.post (.. app -di -$http) "/main/login"
             (js/$.param #js {:username username :password password})
             #js {:headers #js {"Content-Type" "application/x-www-form-urlencoded"}})
      (.success (fn [data]
                  (.fetchStatus app)
                  (.go app "home")))))

(defn logout
  [app]
  (-> (.post (.. app -di -$http) "/main/logout")
      (.success (fn [data]
                  (.fetchStatus app)))))

(defn update-page
  [app message]
  ((.. app -di -notify) "Adding to page"))

(defn handle-message
  [app message]
  (let [notify (.. app -di -notify)
        data (js/JSON.parse (.-data message))]
    (timbre/debug "Received Message: " data)
    (cond
      (.-connection data) (do #_(notify "connected"))
      (.-action data) (condp = (.-action data)
                        "page-add" (update-page app message)
                        (notify "Unknown action type"))
      :default (notify data))))

(defn send
  [app command]
  (.send (.. app -connection) command))

(defn post
  [app activity]
  (timbre/info "Posting Activity" activity)
  (.post (.. app -di -$http) "/model/activities" activity))

(defn get-user
  [app]
  ((.. app -di -$q)
   (fn [resolve reject]
     (if-let [id (.getUserId app)]
       (do (timbre/debug "getting user: " id)
           (resolve (.find (.. app -di -Users) id)))
       (reject nil)))))

(defn following?
  [app target]
  (-> (.getUser app)
      (.then (fn [user]
               (let [response (= (.-_id user) (.-_id target))]
                 (timbre/debug "following?" response)
                 response)))))

(defn follow
  [app target]
  (timbre/debug "follow" target)
  (let [obj  #js {:id (.-_id target)}
        activity #js {:verb "follow" :object obj}]
    (.post app activity)))

(defn unfollow
  [app target]
  (timbre/debug "unfollow" target)
  (let [obj #js {:id (.-_id target)}
        activity #js {:verb "unfollow" :object obj}]
    (.post app activity)))

(defn register
  [app params]
  (timbre/debug "Registering" (.-reg params))
  (let [params #js {:method "post"
                    :url    "/main/register"
                    :data   (.-reg params)}]
    (-> (.$http (.-di app) params)
        (.then (fn [data]
                 (timbre/debug "Response" data)
                 data)))))

(defn get-user-id
  "Returns the authenticated user id from app data"
  [app]
  (let [data (.-data app)]
    (when-let [username (.-user data)]
      (let [domain (.-domain data)]
        (str "acct:" username "@" domain)))))

(defn go
  [app state]
  (.go (.. app -di -$state) state))

(defn add-stream
  [app stream-name]
  (timbre/with-context {:name stream-name}
    (timbre/info "Creating Stream"))
  (let [$http (.. app -di -$http)
        params #js {:name stream-name}]
    (-> (.post $http "/model/streams" params)
        (.then #(.-data %)))))

(def app-methods
  {
   :addStream     add-stream
   :connect       connect
   :getUser       get-user
   :getUserId     get-user-id
   :go            go
   :register      register
   :fetchStatus   fetch-status
   :follow        follow
   :handleMessage handle-message
   :isFollowing   following?
   :login         login
   :logout        logout
   :ping          ping
   :post          post
   :send          send
   :unfollow      unfollow
   })

(defn app-service
  [$http $q $state notify Users $websocket $window DS
   pageService subpageService]
  (timbre/debug "creating app service")
  (let [app #js {}
        data #js {}
        websocket-url (if-let [location (.-location $window)]
                        (str "ws"
                             (when (= (.-protocol location) "https:") "s")
                             "://"
                             (.-host location) "/")
                        (throw (js/Error. "No location available")))
        di #js {:$http $http
                :$q $q
                :DS DS
                :$state $state
                :notify notify
                :Users Users
                :ws $websocket
                :pageService pageService
                :subpageService subpageService}
        connection ($websocket websocket-url)]

    (set! (.-di app) di)
    (set! (.-connection app) connection)
    (set! (.-data app) data)

    (doseq [[n f] app-methods]
      (aset app (name n) (partial f app)))

    (.onMessage connection (.-handleMessage app))

    (.onOpen connection
             (fn []
               (timbre/debug "Connection Opened")))

    (.onClose connection
              (fn []
                (timbre/debug "connection closed")
                (.reconnect connection)))

    (.onError connection
              (fn []
                (timbre/warn "connection errored")))

    (set! (.-app js/window) app)
    ;; return the app
    app))

(def.provider jiksnu.app
  []
  (timbre/debug "initializing app service")
  #js {:$get #js ["$http" "$q" "$state" "notify" "Users" "$websocket" "$window" "DS"
                  "pageService" "subpageService"
                  app-service]})
