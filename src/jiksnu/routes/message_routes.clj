(ns jiksnu.routes.message-routes
  (:require [jiksnu.actions.message-actions :as message]))

(defn routes
  []
  [[[:get    "/:username/inbox"]                             #'message/inbox-page]
   [[:get    "/:username/outbox"]                            #'message/outbox-page]])