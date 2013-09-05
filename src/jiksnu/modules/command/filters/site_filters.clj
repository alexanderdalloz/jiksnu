(ns jiksnu.modules.command.filters.site-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.session :only [current-user-id]]
        [jiksnu.actions.site-actions :as actions.site]))

(deffilter #'actions.site/get-stats :command
  [action request]
  (action))

(deffilter #'actions.site/ping :command
  [action request]
  (apply action (:args request)))

;; (deffilter #'get-load :command
;;   [action request]
;;   (apply action (:args request)))

(deffilter #'actions.site/get-environment :command
  [action request]
  (apply action (:args request)))

(deffilter #'actions.site/get-config :command
  [action request]
  (apply action (:args request)))