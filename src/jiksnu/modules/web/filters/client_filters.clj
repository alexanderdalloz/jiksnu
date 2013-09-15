(ns jiksnu.modules.web.filters.client-filters
  (:require [ciste.config :refer [config]]
            [ciste.filters :refer [deffilter]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.core.filters :refer [parse-page parse-sorting]]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]]))

(deffilter #'actions.client/index :http
  [action request]
  (action))

(deffilter #'actions.client/index :page
  [action request]
  (action))

(deffilter #'actions.client/register :http
  [action request]

  (let [body (slurp (:body request))
        params (merge (:params request)
                      (json/read-str body :key-fn keyword))]
    (action params)))
