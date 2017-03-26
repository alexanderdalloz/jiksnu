(ns jiksnu.pages.LoginPage
  (:require [jiksnu.helpers.page-helpers :as helpers.page
             :refer [by-css by-model]]
            [jiksnu.specs.protocols :refer [Page] :as sp]
            [taoensso.timbre :as timbre]))

(defprotocol LoginPageProto
  (login [this username password])
  (set-password [this password])
  (set-username [this username])
  (submit [this]))

(deftype LoginPage []

  LoginPageProto

  (login [this username password]
    (set-username this username)
    (set-password this password)
    (submit this))

  (set-password [this password]
    (let [locator (by-model "password")]
      (-> (.getWebElement locator)
          (.then (fn [l]
                   (timbre/debugf "Sending password (%s) to %s" password (pr-str l))
                   (.sendKeys locator password))))))

  (set-username [this username]
    (timbre/debug "setting username, login")
    ;; (.sleep js/browser 5000)
    (try
      (.sendKeys (by-model "username") username)
      (catch js/Exception ex
        (timbre/error ex "username error"))))

  (submit [this]
    (timbre/debug "submitting login form")
    (if-let [locator (js/element (by-css "form[name=loginForm]"))]
      (.submit locator)
      (throw (js/Exception. "Could not find login form"))))

  Page

  (load-page [this]
    (timbre/debugf "loading login page")
    (.get js/browser (str helpers.page/base-path "/main/login"))))

(set! (.. LoginPage -prototype -get)
      (fn [] (this-as this (sp/load-page this))))

(set! (.. LoginPage -prototype -waitForLoaded)
      (fn []
        (.wait js/browser
               (fn []
                 (timbre/info "Waiting for loaded")
                 true))))

(set! (.. LoginPage -prototype -loginLink)
      (js/element (by-css ".loginLink")))
