(ns jiksnu.modules.web.views.auth-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.modules.web.sections.auth-sections :as sections.auth]))

(defview #'actions.auth/guest-login :html
  [request user]
  {:status 303
   :template false
   :session {:pending-id (:_id user)}
   :headers {"Location" "/main/password"}})

(defview #'actions.auth/login :html
  [request user]
  (if user
    {:session {:id (:_id user)}
     :status 303
     :template false
     :headers {"Location" "/"}}))

(defview #'actions.auth/logout :html
  [request successful]
  (if successful
    {:session {:id nil}
     :status 303
     :template false
     :headers {"Location" "/"}}))

(defview #'actions.auth/password-page :html
  [request user]
  {:body (sections.auth/password-page user)})