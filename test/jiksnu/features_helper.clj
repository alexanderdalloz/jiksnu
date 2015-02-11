(ns jiksnu.features-helper
  (:require [aleph.http :as http]
            [ciste.config :as c]
            [ciste.core :as core]
            [ciste.runner :as runner]
            [ciste.sections.default :as sections]
            [clj-webdriver.core :as webdriver]
            [clj-webdriver.taxi :as taxi]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            jiksnu.factory
            [jiksnu.mock :refer [my-password]]
            [jiksnu.model :as model]
            [jiksnu.referrant :refer [this that set-this set-that]]
            jiksnu.modules.web.routes
            [jiksnu.session :as session]
            [lamina.core :as l]
            [slingshot.slingshot :refer [throw+ try+]])
  (:import jiksnu.model.Domain
           jiksnu.model.User))

(def server (atom nil))
(defonce loaded (atom false))

(defn after-hook
  []
  (try+
   (log/info "after")
   (ciste.runner/stop-application!)
   (taxi/close)
   (catch Throwable ex
     (log/error ex))))

(defn before-hook
  []
  (when-not @loaded
    (try+
     (let [site-config (ciste.config/load-site-config)]
       (ciste.runner/start-application! :integration)
       (taxi/set-driver! {:browser
                     ;; :chrome
                     :firefox
                     ;; :htmlunit
                     })
       (ciste.loader/process-requires)
       (db/drop-all!)
       (.addShutdownHook
        (Runtime/getRuntime)
        (Thread. (fn [] (after-hook))))
       (dosync
        (ref-set this {})
        (ref-set that {})
        (ref-set my-password nil))
       (dosync
        (swap! loaded (constantly true))))
     (catch Throwable ex
       (.printStackTrace ex)
       (System/exit 0)))))

