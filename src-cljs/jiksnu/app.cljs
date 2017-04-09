(ns jiksnu.app
  (:require [jiksnu.app.loader :as loader]
            [taoensso.timbre :as timbre]))

(defonce models  (atom {}))
(defonce jiksnu (loader/initialize-module!))
