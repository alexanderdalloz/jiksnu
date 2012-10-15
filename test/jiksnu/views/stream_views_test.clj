(ns jiksnu.views.stream-views-test
  (:use [ciste.core :only [with-context with-serialization with-format
                           *serialization* *format*]]
        [ciste.formats :only [format-as]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.session :only [with-user]]
        [jiksnu.test-helper :only [hiccup->doc test-environment-fixture]]
        [jiksnu.actions.stream-actions :only [public-timeline user-timeline]]
        [midje.sweet :only [every-checker fact future-fact => contains truthy]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [net.cgrand.enlive-html :as enlive]))

(test-environment-fixture

 (fact "apply-view #'public-timeline"
   (let [action #'public-timeline]
     (fact "when the serialization is :http"
       (with-serialization :http
         (fact "when the format is :atom"
           (with-format :atom
             (fact "when there are activities"
               (model/drop-all!)
               (let [user (existance/a-user-exists)]
                 (dotimes [n 25]
                   (existance/there-is-an-activity {:user user})))

               (let [request {:action action}
                     response (filter-action action request)]

                 (apply-view request response) =>
                 (every-checker
                  map?
                  #(not (:template %))
                  (fn [response]
                    (let [formatted (format-as :atom request response)
                          feed (abdera/parse-xml-string (:body formatted))]
                      (fact
                        (count (.getEntries feed)) => 20))))))))

         (fact "when the format is :html"
           (with-format :html
             (binding [*dynamic* false]
               (fact "when there are activities"
                 (model/drop-all!)
                 (let [user (existance/a-user-exists)
                       ;; TODO: This used to be set to 25, I need a
                       ;; good way to make sure I have the right
                       ;; amount of records returned in the default
                       ;; page.
                       activities (doall
                                   (for [n (range 20)]
                                     (existance/there-is-an-activity {:user user})))
                       request {:action action}
                       response (filter-action action request)]
                   (apply-view request response) =>
                   (every-checker
                    map?
                    (fn [response]
                      (fact
                        (let [doc (hiccup->doc (:body response))
                              activity-elements (->> [(enlive/attr= :data-model "activity")]
                                                     (enlive/select doc))
                              ids (->> activity-elements
                                       (map #(get-in % [:attrs :data-id]))
                                       (into #{}))]
                          (doseq [activity activities]
                            (let [id (str (:_id activity))]
                              (ids id) => truthy))
                          (count activity-elements) => 20)))))))))))))

 (fact "apply-view #'user-timeline"
   (let [action #'user-timeline]
     (fact "when the serialization is :http"
       (with-serialization :http

         (fact "when the format is :html"
           (with-format :html
             (binding [*dynamic* false]
               (fact "when that user has activities"
                 (model/drop-all!)
                 (let [user (existance/a-user-exists)
                       activity (existance/there-is-an-activity {:user user})
                       request {:action action
                                :params {:id (str (:_id user))}}
                       response (filter-action action request)]
                   (apply-view request response) =>
                   (every-checker
                    (fn [response]
                      (let [doc (hiccup->doc (:body response))]
                        (fact
                          (map
                           #(get-in % [:attrs :data-id])
                           (enlive/select doc [(enlive/attr? :data-id)])) =>
                           (contains (str (:_id activity))))))))))))
         
         (fact "when the format is :n3"
           (with-format :n3
             (fact "when that user has activities"
               (model/drop-all!)
               (let [user (existance/a-user-exists)
                     activity (existance/there-is-an-activity {:user user})
                     request {:action action
                              :params {:id (str (:_id user))}}
                     response (filter-action action request)]
                 (apply-view request response) =>
                 (every-checker
                  map?
                  (fn [response]
                    (fact
                      (let [body (:body response)]
                        body => (partial every? vector?)
                        (let [m (model/triples->model body)]
                          m => truthy)))))))))))))
 
 )
