(ns jiksnu.helpers
  (:require [clojure.string :as string]
            [jiksnu.macros :refer-macros [state-hotkey]]
            [taoensso.timbre :as timbre]))

(defn add-states
  [$stateProvider data]
  (doseq [[state uri controller template] data]
    (.state $stateProvider
            #js
            {:name state
             :url uri
             :controller (str controller "Controller")
             :templateUrl (str "/templates/" (name template))})))

(defn fetch-page
  [$scope $http url]
  (fn []
    (-> $http
        (.get url)
        (.success
         (fn [data]
           (set! (.-page $scope) data))))))

;; TODO: surely this already exists
(defn hyphen-case
  [s]
  (string/lower-case
   (string/replace s #"([a-z])([A-Z])" "$1-$2")))

(defn admin-states
  [data]
  (->> data
       (mapcat
        (fn [[a c]]
          [{:state    (str "admin" a)
            :path     (str "/admin/" (hyphen-case a))
            :class    (str "Admin" a)
            :template (str "admin-" (hyphen-case a))}
           {:state    (str "admin" c)
            :path     (str "/admin/" (hyphen-case a) "/:id")
            :class    (str "Admin" c)
            :template (str "admin-" (hyphen-case c))}]))
       (map (fn [o] (mapv #(% o) [:state :path :class :template])))))

(def admin-data
  [["Activities"    "Activity"]
   ["Conversations" "Conversation"]
   ["Groups"        "Group"]
   ["Resources"     "Resource"]
   ["Users"         "User"]])

(def route-data
  [["avatarPage"            "/main/avatar"             "AvatarPage"            :avatar-page]
   ["home"                  "/"                        "IndexConversations"    :public-timeline]
   ["indexActivities"       "/main/activities"         "IndexActivities"       :index-activities]
   ["indexAlbums"           "/main/albums"             "IndexAlbums"           :index-albums]
   ["indexClients"          "/main/clients"            "IndexClients"          :index-clients]
   ["indexDomains"          "/main/domains"            "IndexDomains"          :index-domains]
   ["indexFeedSources"      "/main/feed-sources"       "IndexFeedSources"      :index-feed-sources]
   ["indexGroups"           "/main/groups"             "IndexGroups"           :index-groups]
   ["indexGroupMemberships" "/main/group-memberships"  "IndexGroupMemberships" :index-group-memberships]
   ["indexLikes"            "/main/likes"              "IndexLikes"            :index-likes]
   ["indexNotifications"    "/main/notifications"      "IndexNotifications"    :index-notifications]
   ["indexPictures"         "/main/pictures"           "IndexPictures"         :index-pictures]
   ["indexResources"        "/main/resources"          "IndexResources"        :index-resources]
   ["indexStreams"          "/main/streams"            "IndexStreams"          :index-streams]
   ["indexSubscriptions"    "/main/subscriptions"      "IndexSubscriptions"    :index-subscriptions]
   ["indexUsers"            "/main/users"              "IndexUsers"            :index-users]
   ["loginPage"             "/main/login"              "LoginPage"             :login-page]
   ["registerPage"          "/main/register"           "RegisterPage"          :register-page]
   ["settingsPage"          "/main/settings"           "SettingsPage"          :settings-page]
   ["showActivity"          "/main/activities/:_id"    "ShowActivity"          :show-activity]
   ["showAlbum"             "/main/albums/:_id"        "ShowAlbum"             :show-album]
   ["showConversation"      "/main/conversations/:_id" "ShowConversation"      :show-conversation]
   ["showDomain"            "/main/domains/:_id"       "ShowDomain"            :show-domain]
   ["showGroup"             "/main/groups/:_id"        "ShowGroup"             :show-group]
   ["showLike"              "/main/likes/:_id"         "ShowLike"              :show-like]
   ["showStream"            "/main/streams/:_id"       "ShowStream"            :show-stream]
   ["showUser"              "/main/users/:_id"         "ShowUser"              :show-user]
   ["authorizeClient"       "/oauth/authorize"         "AuthorizeClient"       :authorize-client]])

(defn setup-hotkeys
  [hotkeys $state]
  (state-hotkey "g a" "indexActivities"       "Go to Activities")
  (state-hotkey "g b" "indexAlbums"           "Go to Albums")
  (state-hotkey "g c" "indexClients"          "Go to Clients")
  (state-hotkey "g d" "indexDomains"          "Go to Domains")
  (state-hotkey "g g" "indexGroups"           "Go to Groups")
  (state-hotkey "g m" "indexGroupMemberships" "Go to Group Memberships")
  (state-hotkey "g n" "indexNotifications"    "Go to Notifications")
  (state-hotkey "g p" "indexPictures"         "Go to Pictures")
  (state-hotkey "g h" "home"                  "Go to Home")
  (state-hotkey "g l" "indexLikes"            "Go to Likes")
  (state-hotkey "g s" "indexStreams"          "Go to Streams")
  (state-hotkey "g u" "indexUsers"            "Go to Users"))

(def states
  (let [as (admin-states admin-data)]
    (concat as route-data)))

(defn fetch-sub-page
  [item subpageService subpage]
  (timbre/debugf "Fetching subpage: %s -> %s" (.-_id item) subpage)
  (-> subpageService
      (.fetch item subpage)
      (.then #(aset item subpage (.-body %)))))

(defn init-item
  [$scope $stateParams app collection]
  (set! (.-init $scope)
        (fn [id]
          (set! (.-loaded $scope) false)
          (.bindOne collection id $scope "item")
          (-> (.find collection id)
              (.then (fn [_] (set! (.-loaded $scope) true))))))
  (set! (.-loaded $scope) false)
  (set! (.-loading $scope) false)
  (set! (.-errored $scope) false)
  (set! (.-app $scope) app)
  (set! (.-refresh $scope) (fn [] (.init $scope (.-id $scope))))
  (set! (.-deleteRecord $scope)
        (fn [item]
          (let [id (.-id $scope)]
            ;; (timbre/debugf "deleting record: %s(%s)" (.getType item) id)
            (-> (.invokeAction app (.-name collection) "delete" id)
                (.then (fn [] (.refresh app)))))))

  (let [id (or (.-id $scope) (.-_id $stateParams))]
    (.init $scope id)))

(defn init-subpage
  [$scope app collection subpage]
  (let []
    (set! (.-app $scope) app)
    (set! (.-loaded $scope) false)
    (set! (.-loading $scope) false)
    (set! (.-errored $scope) false)
    (set! (.-formShown $scope) false)
    (set! (.-toggle $scope) (fn [] (set! (.-formShown $scope) (not (.-formShown $scope)))))

    (.$watch $scope
             #(.-item $scope)
             (fn [item old-item]
               (when (not= item old-item)
                 (if item
                   (.init $scope item subpage)
                   (timbre/warn "item is nil")))))

    (set! (.-refresh $scope) (fn [] (.$broadcast $scope "refresh-page")))

    (set! (.-init $scope)
          (fn [item]
            (let [subpageService (.inject app "subpageService")]
              (timbre/debugf "init subpage %s(%s)=>%s" (.-name collection) (.-_id item) subpage)
              (set! (.-item $scope) item)
              (set! (.-loaded $scope) false)
              (-> (.fetch subpageService item subpage)
                  (.then (fn [page]
                           (timbre/debugf "Subpage resolved - %s(%s)=>%s"
                                          (.-name collection) (.-_id item) subpage)
                           (set! (.-loaded $scope) true)
                           (aset item subpage page))
                         (fn [page]
                           (timbre/debugf "Subpage errored - %s(%s)=>%s"
                                          (.-name collection) (.-_id item) subpage)
                           (set! (.-errored $scope) true)))))))))

(defn init-page
  [$scope $rootScope pageService subpageService page-type]
  (.$on $rootScope "updateCollection" (fn [] (.init $scope)))
  (set! (.-loaded $scope) false)
  (set! (.-init $scope)
        (fn []
          (timbre/debugf "Loading page: %s" page-type)
          (set! (.-loaded $scope) false)
          (-> pageService
              (.fetch page-type)
              (.then (fn [page]
                       (timbre/debugf "Page loaded: %s" page-type)
                       (set! (.-page $scope) page)
                       (set! (.-loaded $scope) true)))))))
