(ns jiksnu.core-spec
  (:require [cljs.nodejs :as nodejs]
            [jiksnu.pages.LoginPage :refer [LoginPage login]]
            [jiksnu.pages.RegisterPage :refer [RegisterPage]]
            [jiksnu.World :refer [browser expect]])
  (:use-macros [jiksnu.step-helpers :only [step-definitions Given When Then And]]))

(defn get-app-data
  "Retrieve the application data"
  []
  (.executeScript browser (fn [] (.-data js/app))))

(defn get-username
  "Retrieve the logged in username from then app service"
  []
  (-> (get-app-data)
      (.then (fn [data]
               (let [username (.-user data)]
                 (js/console.log "Username: %s" username)
                 username)))))

(defn seconds [n] (* n 1000))

(defn register-user
  []
  (.get browser "/main/register"))

(step-definitions

 (js/console.log "loading core spec")

 (this-as this (.setDefaultTimeout this (seconds 60)))

 (Given #"^I am (not )?logged in$" [not-str next]
   (if (empty? not-str)
     (do
       (js/console.log "Logging in user")
       (let [page (LoginPage.)]
        (js/console.log "Fetching Page")
        (.get page)
        (js/console.log "Logging in")
        (login page "test" "test")
        (.waitForAngular browser)
        (js/console.log "Fetching Status")

        (-> (expect (get-username))
            .-to .-eventually (.equal "test"))

        (js/console.log "Expecting title")
        (-> (expect (.getTitle browser))
            .-to .-eventually (.equal "Jiksnu")
            .-and (.notify next))))
     (do
       (js/console.log "Deleting all cookies")
       (.deleteAllCookies (.manage browser))
       (next))))

 (Given #"^there is a user$" []
   (register-user)))
