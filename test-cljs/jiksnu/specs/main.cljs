(ns jiksnu.specs.main
  (:require [cljs.nodejs :as nodejs]
            [jiksnu.step-definitions :as sd]))

(def foo "bar")

(nodejs/enable-util-print!)

(defn -main [& args]
  (println sd/foo)
  (println "Hello world!")
  #_(sd/steps))

(set! *main-cli-fn* -main)

(set! js/module.exports sd/steps)
