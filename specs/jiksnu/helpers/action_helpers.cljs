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

(defn login-user
  "Log in with test user"
  []
  (let [page (LoginPage.)]
    (timbre/info "Fetching login Page")
    (.get page)

    (timbre/info "Logging in")
    (-> (lp/login page "test" "test")
        (.then (fn [] (timbre/info "login finished"))))))

(defn log-out!
  "Delete all browser cookies"
  []
  (timbre/info "Deleting all cookies")
  (js/browser.manage.deleteAllCookies))
