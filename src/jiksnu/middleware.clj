(ns jiksnu.middleware
  (:use [ciste.config :only [config]]
        [clojure.stacktrace :only [print-stack-trace]]
        [jiksnu.session :only [with-user-id]])
  (:require [clojure.tools.logging :as log])
  (:import javax.security.auth.login.LoginException))

(defn wrap-user-binding
  [handler]
  (fn [request]
    (with-user-id (-> request :session :id)
      (handler request))))

(defn wrap-authentication-handler
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch LoginException e
        {:status 303
         :template false
         :flash "You must be logged in to do that."
         :headers {"location" "/main/login"}}))))

(defn wrap-stacktrace
  [handler]
  (fn [request]
    (try
     (handler request)
     (catch Exception ex
       (try
         (let [st (with-out-str (print-stack-trace ex))]
          {:status 500
           :headers {"content-type" "text/plain"}
           :body st})
         (catch Exception ex
           (log/fatalf "Error parsing exception: %s" (str ex))))))))
