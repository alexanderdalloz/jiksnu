(ns jiksnu.actions.comment-actions-test
  (:use [clj-factory.core :only [factory]]
        [midje.sweet :only [=>]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        jiksnu.actions.comment-actions)
  (:require [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]))


(test-environment-fixture

 (context #'fetch-comments
   (context "when the activity exists"
     (context "and there are no comments"
       (let [actor (mock/a-user-exists)
             activity (mock/there-is-an-activity)
             [_ comments] (fetch-comments activity)]
         comments => empty?))))
 )
