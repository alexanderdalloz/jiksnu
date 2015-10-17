(ns jiksnu.modules.http.actions
  (:require [ciste.commands :refer [add-command!]]
            [ciste.core :refer [defaction with-format with-serialization]]
            [ciste.filters :refer [filter-action]]
            [ciste.routes :refer [resolve-routes]]
            [clojure.core.incubator :refer [dissoc-in]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.channels :as ch]
            [jiksnu.handlers :as handler]
            [jiksnu.predicates :as pred]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.util :as util]
            [manifold.bus :as bus]
            [manifold.stream :as s]
            [manifold.time :as lt]
            [org.httpkit.server :as server]
            [slingshot.slingshot :refer [throw+ try+]]))

(defonce connections (ref {}))

(defn all-channels
  []
  (reduce concat (map vals (vals @connections))))

(defn transform-activities
  [connection-id e]
  (let [response {:action "model-updated"
                  :connection-id connection-id
                  :type "activity"
                  :body (:records e)}]
    (bus/publish! ch/events "activities:pushed" response)
    (json/json-str response)))

(defn transform-conversations
  [connection-id e]
  (let [response {:action "page-add"
                  :connection-id connection-id
                  :name "public-timeline"
                  :body (:_id (:records e))}]
    (bus/publish! ch/events "conversations:pushed" response)
    (json/json-str response)))

(defn handle-closed
  [channel status message]
  (let [user-id (:_id (:user status))
        connection-id (:connection status)]
    (log/info "closed connection" user-id connection-id)
    (dosync
     (alter connections #(dissoc-in % [user-id connection-id])))))

(defaction alert-all
  [message]
  (doseq [ch (all-channels)]
    (let [response (json/json-str {:action "add notice"
                                   :message message})]
      (s/put! ch response))))

(defn connect
  [request ch]
  ;; (trace/trace :websocket:connections:established 1)
  (let [user-id (:_id (session/current-user))
        connection-id (util/new-id)
        status {:user user-id :connection connection-id}
        response-channel (s/stream)]

    (log/info "Websocket connection opened" (prn-str status))

    (dosync
     (alter connections #(assoc-in % [user-id connection-id] response-channel)))

    (bus/publish! ch/events :connection-opened status)

    (server/send! ch (str "{connection-id: " connection-id "}"))

    ;; Executes commands for each input
    (server/on-receive ch
                       (fn [body]
                         (when-let [resp (actions.stream/handle-command
                                          response-channel body)]
                           (server/send! ch resp))))
    (server/on-close ch #(handle-closed response-channel status %))

    (s/connect
     (s/map #(transform-activities connection-id %) ch/posted-activities)
     response-channel)

    (s/connect
     (s/map #(transform-conversations connection-id %) ch/posted-conversations)
     response-channel)

    (s/consume #(server/send! ch %) response-channel)

    #_(s/on-closed ch (partial connection-closed user-id connection-id))

    connection-id))
