(ns jiksnu.actions.key-actions-test
  (:require [jiksnu.actions.key-actions :as actions.key]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(th/module-test ["jiksnu.modules.core"])

(fact #'actions.key/index
  (actions.key/index) => map?)
