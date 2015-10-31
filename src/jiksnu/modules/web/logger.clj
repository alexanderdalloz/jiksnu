(ns jiksnu.modules.web.logger
  (:require [active.timbre-logstash :refer [timbre-json-appender]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            jiksnu.util
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :refer [println-appender spit-appender]]
            [taoensso.timbre.profiling :as profiling]
            [socket-rocket.logstash :as sr]
            [fipp.edn :refer (pprint)]
            [puget.printer :as puget]))

(defn json-formatter
  ([data] (json-formatter nil data))
  ([opts data]
   (let [{:keys [instant level ?err_ varargs_
                 output-fn config appender]} data
         out-data {
                   ;; :err (force ?err_)
                   :level level
                   :file (:?file data)
                   :instant instant
                   :message (force (:msg_ data))
                   :varargs (:varargs_ data)
                   ;; :keys (keys data)
                   :line (:?line data)
                   :ns (:?ns-str data)
                   :context (:context data)
                   :hostname (force (:hostname_ data))}]
     ;; (println "")
     ;; (println "")
     (->> out-data
          (map (fn [[k v]] (when v [k v])))
          (into {})
          ;; puget/cprint
          json/json-str))))

(defn set-logger
  []
  (timbre/set-config!
   {:level :debug
    :ns-whitelist []
    :ns-blacklist [
                   "ciste.loader"
                   "ring.timbre.logger"
                   "jiksnu.modules.http.actions"
                   ]
    :middleware []
    :timestamp-opts timbre/default-timestamp-opts
    ;; :output-fn json-formatter
    :appenders
    {
     ;; :json json-appender
     ;; :logstash timbre-json-appender "192.168.1.151" 4660)
     ;; :logstash sr/logstash-appender
     :spit (assoc (spit-appender) :output-fn json-formatter)
     :println (assoc (println-appender {:stream :auto})
                     :min-level :info)}
    :shared-appender-config {:logstash {:port 4660 :logstash "192.168.1.151"}}})
  (timbre/with-context {:foo "bar"}
    (timbre/info "after set")))