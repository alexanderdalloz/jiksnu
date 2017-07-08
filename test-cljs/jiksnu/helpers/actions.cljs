(ns jiksnu.helpers.actions
  (:require [jiksnu.helpers.http :as helpers.http]
            [jiksnu.pages.LoginPage :refer [LoginPage] :as lp]
            [jiksnu.pages.RegisterPage :refer [RegisterPage]]
            [taoensso.timbre :as timbre]))

(defn register-user
  ([] (register-user "test"))
  ([username]
   (timbre/info "Registering user")
   (let [page (RegisterPage.)]
     (.get page)
     #_(.setUsername page username)
     #_(.setPassword page "test")
     (.submit page))))

(defn log-out!
  []
  (do
    (timbre/info "Deleting all cookies")
    (.. js/browser manage deleteAllCookies)))
