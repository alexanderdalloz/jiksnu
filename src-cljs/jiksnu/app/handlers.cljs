(ns jiksnu.app.handlers
  (:require [jiksnu.protocols :as p]
            [jiksnu.app.providers :as providers]
            [jiksnu.app.provider-methods :as methods]))

(defmethod methods/handle-action "page-add"
  [app data]
  (p/update-page app data))
