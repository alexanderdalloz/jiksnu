(ns jiksnu.modules.web.routes
  (:require [cemerick.friend :as friend]
            [cemerick.friend.workflows :as workflows]
            [clojure.tools.logging :as log]
            [compojure.core :refer [GET routes]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.db :refer [_db]]
            [jiksnu.modules.http.resources :refer [defsite groups resources update-groups]]
            [jiksnu.modules.web.middleware :as jm]
            [jiksnu.modules.web.helpers :as helpers]
            [jiksnu.session :as session]
            [octohipster.documenters.schema :refer [schema-doc schema-root-doc]]
            [octohipster.documenters.swagger :refer [swagger-doc]]
            [org.httpkit.server :as server]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [monger.ring.session-store :as ms]))

(def auth-config
  {:credential-fn actions.auth/check-credentials
   :login-uri "/main/login"
   :workflows [(workflows/http-basic :realm "/")
               (workflows/interactive-form)]})

(defn async-handler
  [request]
  (when (:websocket? request)
    (server/with-channel request channel
      (server/on-receive channel
                         (fn [body]
                           (when-let [resp (actions.stream/handle-command
                                            request channel body)]
                             (server/send! channel resp))))
      (server/on-close channel
                       (fn [status]
                         (actions.stream/handle-closed request channel status))))))

(defsite jiksnu
  :description "Jiksnu Social Networking"
  :schemes ["http"]
  :documenters [swagger-doc schema-doc schema-root-doc])

(def app
  (-> (routes
       async-handler
       (route/resources "/")
       (GET "/templates/*" [] #'helpers/serve-template)
       (-> #'jiksnu-routes
           (friend/authenticate auth-config)
           (handler/site {:session {:store (ms/session-store @_db "session")}})))
      wrap-file-info
      wrap-content-type
      wrap-not-modified))
