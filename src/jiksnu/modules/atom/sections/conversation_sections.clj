(ns jiksnu.modules.atom.sections.conversation-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [delete-button full-uri uri title index-line
                                       index-block index-line index-section link-to
                                       show-section update-button]]
        [jiksnu.ko :only [*dynamic*]]
)
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.namespace :as ns]
            [jiksnu.session :as session])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation
           jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.User))

;; index-section

(defsection index-section [Conversation :atom]
  [items & [page]]
  (let [ids (map :_id items)
        page (actions.activity/fetch-by-conversations ids)]
    (index-block (:items page) page)))
