(ns jiksnu.modules.http.views.conversation-views-test
  (:require [ciste.core :refer [with-context]]
            [ciste.filters :refer [filter-action]]
            [ciste.views :refer [apply-view]]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "apply-view #'actions.conversation/index [:http :viewmodel]"
  (let [action #'actions.conversation/index]
    (with-context [:http :viewmodel]
      (fact "when there are no conversations"
        (let [request {:action action
                       :format :viewmodel
                       :params {:format :viewmodel}}
              response (filter-action action request)
              rendered (apply-view request response)]

          (fact "returns a map"
            rendered => map?)))
      )
    ))


