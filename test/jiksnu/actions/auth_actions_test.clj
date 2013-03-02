(ns jiksnu.actions.auth-actions-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.actions.auth-actions :only [login]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [=> contains fact truthy]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.mock :as mock]))

(test-environment-fixture

 (fact "#'login"
   (let [password (fseq :password)
         user (mock/a-user-exists {:password password})]
     (login user password) => truthy))

 )
