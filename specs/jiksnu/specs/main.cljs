(ns jiksnu.specs.main
  (:require [cljs.nodejs :as nodejs]
            [jiksnu.step-definitions :as sd]))

(nodejs/enable-util-print!)
(defn -main [& args]
  (sd/steps))
(set! *main-cli-fn* -main)
