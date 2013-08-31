(ns jiksnu.actions.request-token-actions-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.actions.request-token-actions :as actions.request-token]
            [jiksnu.mock :as mock]
            [jiksnu.session :as session]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [midje.sweet :refer [=>]])
  (:import jiksnu.model.RequestToken
           org.bson.types.ObjectId
           ))

(test-environment-fixture

 (context #'actions.request-token/create
   (let [params {}]
     (actions.request-token/create params) =>
     (check [token]
       token => (partial instance? RequestToken)
       (:_id token) => (partial instance? ObjectId)
       )
     )
   )

 (context #'actions.request-token/parse-authorization-header
   (let [callback "oob"
         signature-method "HMAC-SHA1"
         consumer-key "foo"
         version "1.0"
         timestamp "1377743340"
         nonce "323279719"
         signature "SaWV7wRJESUXUzT6FaLpTH2upeg%3D"
         header (format (str "OAuth "
                             "oauth_callback=\"%s\", "
                             "oauth_signature_method=\"%s\", "
                             "oauth_consumer_key=\"%s\", "
                             "oauth_version=\"%s\", "
                             "oauth_timestamp=\"%s\", "
                             "oauth_nonce=\"%s\", "
                             "oauth_signature=\"%s\"")
                        callback signature-method consumer-key version timestamp
                        nonce signature)
         auth-map {"oauth_callback" callback
                   "oauth_signature_method" signature-method
                   "oauth_consumer_key" consumer-key
                   "oauth_version" version
                   "oauth_timestamp" timestamp
                   "oauth_nonce" nonce
                   "oauth_signature" signature}]
     (actions.request-token/parse-authorization-header header) =>
     (check [[type params]]
       type => "OAuth"
       params => auth-map)))

 (context #'actions.request-token/get-request-token
   (let [params {}]
     (actions.request-token/get-request-token params) =>
     (check [token]
       token => (partial instance? RequestToken)
       )
     )
   )

 )



