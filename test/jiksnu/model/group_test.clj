(ns jiksnu.model.group-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
        [jiksnu.model.group :only [create delete fetch-all fetch-by-id]]
        [midje.sweet :only [fact => every-checker future-fact]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.group :as model.group]
            [jiksnu.util :as util]))

(test-environment-fixture

 (context #'fetch-by-id
   (context "when the item doesn't exist"
     (let [id (util/make-id)]
       (fetch-by-id id) => nil?))

   (context "when the item exists"
     (let [item (mock/a-group-exists)]
       (fetch-by-id (:_id item)) => item)))

 (context #'delete
   (context "when the item exists"
     (let [item (mock/a-group-exists)]
       (delete item) =>  item)))

 (context #'fetch-all
   (context "when there are no items"
     (db/drop-all!)
     (fetch-all) =>
     (every-checker
      empty?))

   (context "when there are less than a page"
     (db/drop-all!)
     (dotimes [n 19]
       (actions.group/create (factory :group)))
     (fetch-all) =>
     (every-checker
      seq?
      #(fact (count %) => 19)))

   (context "when there is more than a page"
     (db/drop-all!)
     (dotimes [n 21]
       (actions.group/create (factory :group)))
     (fetch-all) =>
     (every-checker
      seq?
      #(fact (count %) => 20))))

 )
