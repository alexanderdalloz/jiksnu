(ns jiksnu.views.admin.feed-subscription-views-test
  (:use [ciste.core :only [with-serialization with-format]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [every-checker fact future-fact => contains]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.actions.admin.feed-subscription-actions :as actions.admin.feed-subscription]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.user :as model.user]
            jiksnu.views.stream-views))

(test-environment-fixture
 (fact "apply-view #'actions.admin.feed-subscription/index"
   (let [action #'actions.admin.feed-subscription/index]
     (fact "when the serialization is :http"
       (with-serialization :http
         (fact "when the format is :html"
           (with-format :html
             (let [feed-subscription (model.feed-subscription/create (factory :feed-subscription))]
               (let [request {:action action}
                     response (filter-action action request)]
                 (apply-view request response) =>
                 (every-checker
                  map?
                  (comp status/success? :status)
                  (fn [result]
                    (let [body (h/html (:body result))]
                      (fact
                        body => (re-pattern (str (:_id feed-subscription)))))))))))))))
 )