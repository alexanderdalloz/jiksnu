(ns jiksnu.test-helper
  (:use [ciste.config :only [load-site-config]]
        [ciste.loader :only [process-requires]]
        [ciste.runner :only [start-application! stop-application!]]
        [slingshot.slingshot :only [try+]])
  (:require [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model :as model]
            [jiksnu.referrant :as r]
            [net.cgrand.enlive-html :as enlive])
  (:import java.io.StringReader))

(defn hiccup->doc
  [hiccup-seq]
  (-> hiccup-seq
      h/html
      StringReader.
      enlive/html-resource))

(defmacro test-environment-fixture
  [& body]
  `(do
     (println "****************************************************************************")
     (println (str "Testing " *ns*))
     (println "****************************************************************************")
     (println " ")
     (load-site-config)
     (start-application! :test)

     (model/drop-all!)
     (actions.domain/current-domain)

     (dosync
      (ref-set r/this {})
      (ref-set r/that {}))

     ~@body
     (stop-application!)))
