(ns jiksnu.core
  (:use [lolg :only [start-display console-output]]
        [jayq.core :only [$ css inner prepend text]])
  (:require [clojure.browser.repl :as repl]
            [goog.events :as events]
            [goog.dom :as dom]
            [goog.net.XhrIo :as xhrio]
            [jiksnu.logging :as log]
            [waltz.state :as state]
            [jayq.core :as jayq]
            [jiksnu.websocket :as ws])
  (:use-macros [waltz.macros :only [in out defstate defevent]]))

(defn greet
  [n]
  (str "Hello " n))

(defn find-parent-article
  [element]
  (let [helper (goog.dom.DomHelper.)]
    (.getAncestorByTagNameAndClass helper element "article" "notice")))

(defn position-handler
  [position]
  (let [coords (. position (coords))]
    (log/info (str "lat: " (. coords (latitude))))
    (log/info (str "long: " (. coords (longitude))))))

(defn halt
  [event]
  (. event (stopPropagation))
  (. event (preventDefault)))

(defn do-delete-activity
  [x]
  (log/info "Delete button clicked")
  (log/info x)
  (if-let [article (find-parent-article (. x (target)))]
    (let [id (.getAttribute article "id")
          url (str "/notice/" id)]
      (xhrio/send
       url
       (fn [e]
         (log/info e)
         #_(.hide this))
       "DELETE")
      (halt x))
    (log/info "article not found")))

(defn do-like-button
  [x]
  (log/info "like button clicked")
  #_(halt x))

(defn add-handler
  [handler elements]
  (doseq [i (range (alength elements))]
    (let [element (aget elements i)]
      (events/listen element "click" handler)
      (events/listen element goog.events.EventType/SUBMIT handler))))

(defn do-logout-link
  [event]
  (console/log "Logging out")
  #_(halt event))

(defn connect-repl
  []
  (repl/connect "http://192.168.1.42:9001/repl"))

(defn setup-handlers
  []
  (add-handler do-delete-activity ($ :.delete-button))
  (add-handler do-like-button ($ :.like-button))
  (add-handler do-logout-link ($ :.logout-link)))

(defn update-statistics
  [stats]
  (let [stat-section ($ :.statistics-section)]
    (doseq [key (keys stats)]
      (let [finder (format "*[data-model='%s']" key)
            section (jayq/find stat-section finder)]
        (.effect section "highlight" 3000)
        (jayq/text (jayq/find section :.stat-value)
                   (get stats key))))))

(defn main
  []
  (start-display (console-output))
  (log/info "starting application")
  (setup-handlers)
  (state/trigger ws/ws-state :connect)
  (update-statistics {"users" 9001
                      "activities" "123456"}))

(main)
;; (connect-repl)
