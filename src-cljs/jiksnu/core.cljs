(ns jiksnu.core
  (:require
   [goog.events :as events]
   [goog.dom :as dom]
   ;; [goog.dom.query :as query]
   [goog.net.XhrIo :as xhrio]
   ;; [goog.net.WebSocket :as ws]
    
   )

  )

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
    (console/log "lat: " (. coords (latitude)))
    (console/log "long: " (. coords (longitude)))))

(defn do-delete-activity
  [x]
  (console/log "Delete button clicked")
  (console/log x)
  (if-let [article (find-parent-article (. x (target)))]
    (let [id (.getAttribute article "id")
          url (str "/notice/" id)]
      (xhrio/send
       url
       (fn [e]
         (console/log e)
         #_(.hide this))
       "DELETE")
      (. x (stopPropagation))
      (. x (preventDefault)))
    (console/log "article not found")))

(defn do-like-button
  [x]
  (console/log "like button clicked")

  (. x (stopPropagation))
  (. x (preventDefault)))

(defn add-handler
  [handler elements]
  (doseq [i (range (alength elements))]
    (let [element (aget elements i)]
      (events/listen element "click" handler)
      (events/listen element goog.events.EventType/SUBMIT handler))))

(defn find-element
  [selector]
  (dom/getElementsByClass selector))

(defn -main
  []
  (console/log "starting application")
  ;; (add-handler do-delete-activity (dom/query "#delete-button"))
  ;; (add-handler do-like-button (dom/query "#like-button"))

  )

(-main)