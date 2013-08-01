(ns jiksnu.routes.salmon-routes
  (:use [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.actions.salmon-actions :as salmon]))

(add-route! "/main/salmon/user/:id" {:named "user salmon"})

(defn routes
  []
  [[[:post (named-path "user salmon")] #'salmon/process]])

