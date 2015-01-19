(ns jiksnu.modules.http.views.activity-views-test
  (:require [ciste.core :refer [with-context]]
            [ciste.filters :refer [filter-action]]
            [ciste.views :refer [apply-view]]
            [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [midje.sweet :refer [=> =not=> contains fact facts]]))

(test-environment-fixture

 (fact "apply-view #'actions.activity/oembed [:http :json]"
   (let [action #'actions.activity/oembed]
     (with-context [:http :json]
       (let [activity (mock/there-is-an-activity)
             request {:params {:url (:id activity)}
                      :action action}
             response (filter-action action request)]
         (let [response (apply-view request response)]
           (let [body (:body response)]
             response => map?
             (:status response) => status/success?
             body => (contains {:title (:title activity)})))))))

 (fact "apply-view #'actions.activity/oembed [:http :xml]"
   (let [action #'actions.activity/oembed]
     (with-context [:http :xml]
       (let [activity (mock/there-is-an-activity)
             request {:params {:url (:id activity)}
                      :action action}
             item {} #_(filter-action action request)]
         (let [response (apply-view request item)]
           (let [body (:body response)]
             response => map?
             (:status response) => status/success?
             body =not=> string?))))))

 )
