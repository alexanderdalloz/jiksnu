(ns jiksnu.helpers.features
  (:require [clj-webdriver.taxi :as taxi]
            [jiksnu.db :as db]
            [jiksnu.mock :refer [my-password]]
            [jiksnu.referrant :refer [this that set-this set-that]]
            [slingshot.slingshot :refer [throw+ try+]]
            [taoensso.timbre :as timbre]
            [clj-webdriver.driver :as driver])
  (:import (org.openqa.selenium.remote DesiredCapabilities CapabilityType RemoteWebDriver)
           (java.net URL)))

(def server (atom nil))
(defonce loaded (atom false))
(def driver (atom nil))

(defn get-selenium-config
  []
  (let [host "selenium"
        port 24444
        url (str "http://" host ":" port "/wd/hub")]
    {:host host
     :port port
     :url url}))

(defn restart-session
  []
  (when (not @driver)
    (let [{:keys [url]} (get-selenium-config)
          caps (doto (DesiredCapabilities.)
                 (.setCapability CapabilityType/BROWSER_NAME "firefox")
                 ;(.setCapability CapabilityType/PLATFORM Platform/MAC)
                 (.setCapability "name" "clj-webdriver-test-suite"))
          wd (RemoteWebDriver. (URL. url) caps)]
      (timbre/debug "Getting connection")
      (let [session-id (str (.getSessionId wd))]
        (timbre/infof "Session Id: %s" session-id)
        (reset! driver wd)))))

(defn after-hook
  []
  (try+
   (timbre/info "after")
   #_(ciste.runner/stop-application!)
   (taxi/close)
   (catch Throwable ex
     (timbre/error ex))))

 (defn before-hook
   []
   (timbre/info "before")
   (when-not @loaded
    (try+
      (restart-session)
      (let [d (driver/init-driver @driver)]
        (timbre/infof "driver: %s" (driver/driver? d))
        (taxi/set-driver! d))
      ;(ciste.runner/start-application! :integration)
      ;(taxi/set-driver! {:browser
      ;                   ;; :chrome
      ;                   :firefox
      ;                   ;; :htmlunit
      ;                   })
      ;(ciste.loader/process-requires)
      ;(db/drop-all!)
      (.addShutdownHook
        (Runtime/getRuntime)
        (Thread. (fn [] (after-hook))))
      (dosync
        (ref-set this {})
        (ref-set that {})
        (ref-set my-password nil))
      (dosync
        (swap! loaded (constantly true)))
      (catch Throwable ex
        (.printStackTrace ex)
        (System/exit 0)))))
