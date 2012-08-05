(ns jiksnu.triggers.subscription-triggers-test
  (:use [ciste.config :only [with-environment]]
        clj-factory.core
        jiksnu.test-helper
        jiksnu.triggers.subscription-triggers
        midje.sweet)
  (:require [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

(test-environment-fixture

 (future-fact "notify-subscribe-xmpp"
   (fact "should return a packet"
     (let [user (model.user/create (factory :user))
           subscribee (model.user/create (factory :user))
           subscription (model.subscription/subscribe
                         (:_id user) (:_id subscribee))]
       (notify-subscribe-xmpp {:id "JIKSNU1"} subscription) => packet/packet?))))
