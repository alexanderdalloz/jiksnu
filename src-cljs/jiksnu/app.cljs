(ns jiksnu.app
  (:require [jiksnu.model :as model]
            [jiksnu.registry :as registry]
            [jiksnu.app.loader :as loader]
            [jiksnu.app.providers :as providers]
            [taoensso.timbre :as timbre]))

(defonce models  (atom {}))

(defonce jiksnu (loader/initialize-module! plugins))

(.provider jiksnu "app" providers/app)

(-> jiksnu
    (.factory "Activities"       #js ["DS" Activities])
    (.factory "Albums"           #js ["DS" Albums])
    (.factory "Clients"          #js ["DS" Clients])
    (.factory "Conversations"    #js ["DS" "subpageService" Conversations])
    (.factory "Domains"          #js ["DS" Domains])
    (.factory "FeedSources"      #js ["DS" FeedSources])
    (.factory "Followings"       #js ["DS" Followings])
    (.factory "Groups"           #js ["DS" Groups])
    (.factory "GroupMemberships" #js ["DS" GroupMemberships])
    (.factory "Likes"            #js ["DS" Likes])
    (.factory "Notifications"    #js ["DS" Notifications])
    (.factory "Pages"            #js ["DS" Pages])
    (.factory "Pictures"         #js ["DS" Pictures])
    (.factory "RequestTokens"    #js ["DS" RequestTokens])
    (.factory "Resources"        #js ["DS" Resources])
    (.factory "Services"         #js ["DS" Services])
    (.factory "Streams"          #js ["DS" Streams])
    (.factory "Subscriptions"    #js ["DS" Subscriptions])
    (.factory "Users"            #js ["DS" "subpageService" Users]))

(.controller jiksnu "AppController" #js [(fn [])])
