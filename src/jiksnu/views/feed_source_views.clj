(ns jiksnu.views.feed-source-views
  (:use [ciste.config :only [config]]
        [ciste.views :only [defview]]
        ciste.sections.default
        [clojurewerkz.route-one.core :only [named-path]]
        jiksnu.actions.feed-source-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [format-page-info pagination-links with-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]
            [ring.util.response :as response])
  (:import jiksnu.model.Activity
           jiksnu.model.FeedSource))

;; process-updates

(defview #'process-updates :html
  [request params]
  {:body params
   :template false})

;; remove-subscription

(defview #'remove-subscription :html
  [request params]
  (-> (named-path "index feed-sources")
      response/redirect-after-post
      (assoc :template false)))

;; show

(defn bind-to
  [property & body]
  (concat
   [:div {:data-bind (str "with: " property)}]
   body))

(defview #'show :html
  [request item]
  {:body
   [:div (if *dynamic*
           {:data-bind "with: targetFeedSource"})
    (let [activity (if *dynamic*
                     (FeedSource.)
                     item)]
      (list (show-section item)
            (with-page "activities"
              (let [page (actions.activity/fetch-by-feed-source item)
                    items (if *dynamic* [(Activity.)] (:items page))]
                (list
                 (pagination-links (if *dynamic* {} page))
                 [:div {:data-bind "with: items"}
                  (index-section items)])))))]
   :viewmodel (str (named-path "show feed-source" {:id (:_id item)}) ".viewmodel")})

(defview #'show :model
  [request activity]
  {:body (show-section activity)})

(defview #'show :viewmodel
  [request item]
  {:body {:targetFeedSource (:_id item)
          :title (:title item)
          :pages {:activities
                  (let [page (actions.activity/fetch-by-feed-source item
                                                                    {:sort-clause {:updated 1}})]
                    (format-page-info page))}}})

;; update

(defview #'update :html
  [request params]
  (-> (named-path "index feed-sources")
      response/redirect-after-post
      (assoc :template false)))
