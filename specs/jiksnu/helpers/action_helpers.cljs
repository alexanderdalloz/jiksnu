(ns jiksnu.helpers.action-helpers
  (:require [jiksnu.helpers.http-helpers :as helpers.http]
            [jiksnu.pages.LoginPage :refer [LoginPage] :as lp]
            [jiksnu.pages.RegisterPage :refer [RegisterPage]]
            [taoensso.timbre :as timbre]))

(defn register-user
  "Register a new user"
  ([] (register-user "test"))
  ([username]
   (timbre/infof "Registering user: %s" username)
   (let [page (RegisterPage.)]
     (-> (.get page)
         (.then (fn [] (.setUsername page username)))
         (.then (fn [] (.setPassword page "test")))
         (.then (fn [] (.submit page)))
         (.then (fn []
                  (timbre/debugf "Asserting user exists: %s" username)
                  (helpers.http/user-exists? username)))))))

(defn log-out!
  "Delete all browser cookies"
  []
  (timbre/info "Deleting all cookies")
  #_(js/browser.manage.deleteAllCookies))
