(ns jiksnu.model.webfinger-test
  (:use [ciste.config :only [with-environment]]
        [clj-factory.core :only [factory]]
        midje.sweet
        jiksnu.test-helper
        jiksnu.model
        jiksnu.model.webfinger)
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user])
  (:import jiksnu.model.User
           nu.xom.Document))

(test-environment-fixture

 ;; TODO: Mock these, don't actually request
 (fact "#'fetch-host-meta"
   (fact "when the url points to a valid XRD document"
     ;; TODO: pick a random domain
     (let [url "http://kronkltd.net/.well-known/host-meta"]
       (fetch-host-meta .url.) => (partial instance? Document))
     (provided
       (cm/fetch-resource .url.) => "<XRD/>"))
   
   (fact "when the url does not point to a valid XRD document"
     (let [url "http://example.com/.well-known/host-meta"]
       (fetch-host-meta .url.) => (throws Exception))
     (provided
       (cm/fetch-resource .url.) => "")))
 
 (future-fact "#'get-links"
   (fact "When it has links"
     (let [xrd nil]
       (get-links xrd)) => seq?))

 )
