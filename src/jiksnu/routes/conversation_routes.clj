(ns jiksnu.routes.conversation-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]])
  (:require [jiksnu.actions.conversation-actions :as conversation]))

(add-route! "/main/conversations"     {:named "conversation index"})
(add-route! "/main/conversations/:id" {:named "show conversation"})

(defn routes
  []
  [[[:get (named-path "conversation index")]                  #'conversation/index]
   [[:get (str (named-path "conversation index") ".:format")] #'conversation/index]
   [[:get (named-path "show conversation")]                   #'conversation/show]
   [[:get (str (named-path "show conversation") ".:format")]  #'conversation/show]])
