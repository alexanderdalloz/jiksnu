(ns jiksnu.features-helper
  (:use aleph.http
        aleph.formats
        [ciste.model :only [implement]]
        [clj-factory.core :only [factory fseq]]
        clj-webdriver.taxi
        [clojure.core.incubator :only [-?>]]
        [lamina.core :only [permanent-channel read-channel* siphon]]
        [lamina.executor :only [task]]
        midje.sweet
        ring.mock.request
        [slingshot.slingshot :only [throw+]])
  (:require [aleph.http :as http]
            [ciste.config :as c]
            [ciste.core :as core]
            [ciste.runner :as runner]
            [ciste.sections.default :as sections]
            [ciste.service.aleph :as aleph]
            [clj-http.client :as client]
            [clj-webdriver.core :as webdriver]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.user-actions :as actions.user]
            jiksnu.factory
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            jiksnu.routes
            [jiksnu.session :as session]
            [ring.mock.request :as mock])
  (:import jiksnu.model.Activity
           jiksnu.model.Domain
           jiksnu.model.User
           org.openqa.selenium.NoSuchElementException))

(def server (atom nil))
(def current-page (ref nil))
(def domain "localhost")
(def port 8175)

(def this (ref {}))
(def that (ref {}))

(defn get-this
  [k]
  (get @this k))

(defn set-this
  [k v]
  (dosync
   (alter this assoc k v)))

(defn get-that
  [k]
  (get @that k))

(defn set-that
  [k v]
  (dosync
   (alter that assoc k v)))

(def that-stream (permanent-channel))
(def my-password (ref nil))


(defn before-hook
  []
  (try (let [site-config (ciste.config/load-site-config)]
         
         (ciste.runner/start-application! :integration)
         (set-driver! {:browser
                       ;; :firefox
                       :htmlunit})
         (ciste.loader/process-requires)
         (model/drop-all!)
         (dosync
          (ref-set this {})
          (ref-set that {})
          (ref-set my-password nil)))
       (catch Exception ex
         (.printStackTrace ex)
         (System/exit 0))))

(defn after-hook
  []
  (try
    (log/info "after")
    (ciste.runner/stop-application!)
    (catch Exception ex
      (log/error ex))))

(defmacro check-response
  [& body]
  `(and (not (fact ~@body))
        (throw (RuntimeException. "failed"))))

(defn expand-url
  [path]
  (str "http://" domain
       (if-not (= port 80)
         (str ":" port)) path))

(def page-names
  {
   "home"                           "/"
   "login"                          "/main/login"
   "ostatus sub"                    "/main/ostatussub"
   "host-meta"                      "/.well-known/host-meta"
   "subscription index"             "/admin/subscriptions"
   "edit profile"                   "/main/profile"
   "user admin"                     "/admin/users"
   "user index"                     "/users"
   "domain index"                   "/main/domains"
   "feed source admin index"        "/admin/feed-sources"
   "feed subscriptions admin index" "/admin/feed-subscriptions"
   "like admin index"               "/admin/likes"
   "subscriptions admin index"      "/admin/subscriptions"
   "firehose"                       "/main/events"})

(defn fetch-page
  [method path]
  (let [request {:method method
                 :url (expand-url path)}
        response (sync-http-request request)]
    (dosync
     (ref-set current-page response))))

(defn fetch-page-browser
  [method path]
  (to (expand-url path)))

(defn get-body
  []
  (-> @current-page :body channel-buffer->string))






(declare a-user-exists do-login)


(defn a-domain-exists
  []
  (let [domain (model.domain/create (factory :domain))]
    (set-this :domain domain)
    domain))

(defn a-feed-source-exists
  [& [options]]
  (let [domain (or (:domain options)
                   (get-this :domain)
                   (a-domain-exists))
        source (actions.feed-source/create
                (factory :feed-source
                         {:topic (format "http://%s/api/statuses/user_timeline/1.atom" (:_id domain))
                          :hub (format "http://%s/push/hub" (:_id domain))}))]
    (set-this :feed-source source)
    source))

(defn a-feed-subscription-exists
  [& [options]]
  (let [domain (or (:domain options)
                   (get-this :domain)
                   (a-domain-exists))
        feed-subscription (model.feed-subscription/create
                           (factory :feed-subscription
                                    {:domain domain}))]
    (set-this :feed-subscription feed-subscription)
    feed-subscription))

(defn a-normal-user-is-logged-in
  []
  (a-user-exists)
  (do-login))

(defn a-record-exists
  [type]
  (let [ns-sym (symbol (format "jiksnu.actions.%s-actions"
                               (name type)))]
    (require ns-sym)
    (if-let [create-fn (ns-resolve (the-ns ns-sym) 'create)]
      (->> (factory type)
           create-fn
           (set-this type))
      (throw+ (format "could not find %s/create" ns-sym)))))

(defn a-subscription-exists
  []
  (->> (factory :subscription)
       model.subscription/create
       (set-this :subscription)))

(defn a-user-exists
  ([] (a-user-exists {:discovered true} "hunter2"))
  ([options]
     (a-user-exists options "hunter2"))
  ([opts password]
     (let [user (actions.user/register
                 {:username (fseq :username)
                  :password password
                  :display-name (fseq :name)
                  :accepted true})
           user (if (:admin opts)
                  (do (model.user/set-field! user :admin true)
                      (assoc user :admin true))
                  user)]
       (set-this :user user)
       (dosync
        (ref-set my-password password))
       user)))

(defn a-user-exists-with-password
  [password]
  (a-user-exists {} password))

(defn another-user-exists
  [& [options]]
  (log/info "another user")
  (let [domain (or (:domain options)
                   (get-that :domain)
                   (a-domain-exists))
        source (or (:source options)
                   (get-that :feed-source)
                   (a-feed-source-exists {:domain domain}))
        user (actions.user/create (factory :user
                                           {:domain (:_id domain)
                                            :update-source (:_id source)}))]
    (set-that :user user)
    user))

(defn activity-gets-posted
  [& [options]]
  (let [source (or (:feed-source options)
                   (get-this :feed-source)
                   (a-feed-source-exists))
        activity (actions.activity/post (factory :activity
                                                 {:update-source source}))]
    (set-this :activity activity)
    activity))

(defn alias-should-match-uri
  []
  (check-response
   (let [uri (model.user/get-uri (get-this :user))
         pattern (re-pattern (str ".*" uri ".*"))]
     (get-body) => pattern)))

(defn am-not-logged-in
  []
  nil)

(defn an-admin-is-logged-in
  []
  (a-user-exists)
  (-> (get-this :user)
      (assoc :admin true)
      actions.user/update
      session/set-authenticated-user!)
  (do-login))

(defn be-at-the-page
  [page-name]
  (let [path (get page-names page-name)]
    (fetch-page-browser :get path)))

(defn be-at-the-page-for-domain
  [page-name]
  (condp = page-name
    "show"
    (check-response
     (let [url (:_id (get-this :domain))]
       ;; TODO: Identify the domain link
       (find-element url) => truthy))))

(defn do-click-button
  [class-name]
  (click (str "#" class-name "-button")))

(defn do-click-button-for-this-type
  [button-name type]
  (if-let [record (get-this type)]
    (let [button (find-element-under
                  (str "*[data-id='" (:_id record) "']")
                  (webdriver/by-class-name (str button-name "-button")))]
      (click button))
    (throw+ (format "Could not find 'this' record for %s" type))))

(defn do-click-button-for-that-type
  [button-name type]
  (if-let [record (get-that type)]
    (let [button (find-element-under
                  (str "*[data-id='" (:_id record) "']")
                  (webdriver/by-class-name (str button-name "-button")))]
      (click button))
    (throw+ (format "Could not find 'that' record for %s" type))))

(defn do-click-link
  [value]
  (click (str "*[value='" value "']")))

(defn do-enter-field
  [value field-name]
  (let [selector (str "*[name='" field-name "']")]
    (try (clear selector)
         (input-text selector value)
         (catch NullPointerException ex
           (throw+ (str "Could not find element with selector: " selector))))))

(defn do-enter-password
  []
  (do-enter-field @my-password "password"))

(defn do-enter-username
  []
  (do-enter-field (:username (get-this :user)) "username"))

(defn do-login
  []
  (to (expand-url "/main/login"))

  (do-enter-username)
  (do-enter-password)
  (click "input[type='submit']")
  (session/set-authenticated-user! (get-this :user)))

(defn do-wait
  []
  (Thread/sleep 5000))

(defn do-wait-forever
  []
  @(promise))

(defn domain-should-be-deleted
  []
  (check-response
   (actions.domain/show (get-this :domain)) => nil))

(defn domain-should-be-discovered
  []
  (check-response
   (get-this :domain) => (contains {:discovered true})))

(defn fetch-user-meta-for-user
  []
  (fetch-page-browser
   :get
   (str "/main/xrd?uri=" (model.user/get-uri (get-this :user)))))

(defn fetch-user-meta-for-user-with-client
  []
  (fetch-page :get "/.well-known/host-meta"))

(defn get-not-found-error
  []
  (check-response
   (page-source) => #"Not Found"))

(defn go-to-the-page
  [page-name]
  (if-let [path (get page-names page-name)]
    (fetch-page-browser :get path)
    (throw (RuntimeException. (str "No path defined for " page-name)))))

(defn go-to-the-page-for-activity
  [page-name]
  (condp = page-name
    "show" (core/with-context [:html :http]
             (let [path (sections/uri (get-this :activity))]
               (fetch-page-browser :get path)))))

(defn go-to-the-page-for-domain
  [page-name]
  (condp = page-name
    "show" (let [path (str "/main/domains/" (:_id (get-this :domain)))]
             (fetch-page-browser :get path))
    (implement)))

(defn go-to-the-page-for-user
  [page-name user format]
  (if-let [path (condp = page-name
                  "show"          (str "/main/users/" (:_id user))
                  "user timeline" (str "/remote-user/" (:username user) "@" (:domain user))
                  "subscriptions" (str "/" (:username user) "/subscriptions")
                  "subscribers"   (str "/" (:username user) "/subscribers")
                  nil)]
    (fetch-page-browser :get
                        (if format
                          (str path "." format)
                          path))
    (implement)))

(defn go-to-the-page-for-this-user
  ([page-name]
     (go-to-the-page-for-this-user page-name nil))
  ([page-name format]
     (let [user (get-this :user)]
       (go-to-the-page-for-user page-name user format))))

(defn go-to-the-page-for-that-user
  ([page-name]
     (go-to-the-page-for-that-user page-name nil))
  ([page-name format]
     (let [user (get-that :user)]
       (go-to-the-page-for-user page-name user format))))

(defn host-field-should-match-domain
  []
  (check-response
   (let [domain (c/config :domain)
         pattern (re-pattern (str ".*" domain ".*"))]
     (get-body) => pattern)))

(defn log-response
  []
  (-> @current-page :body channel-buffer->string log/info))

(defn name-should-be
  [display-name]
  (check-response
   (model.user/fetch-by-id (:_id (get-this :user))) => (contains {:display-name display-name})))

(defn request-oembed-resource
  []
  (fetch-page-browser :get (str "/main/oembed?format=json&url=" (:url (get-this :activity)))))

(defn request-stream
  [stream-name]
  (let [ch (:body @(http/http-request
                    (mock/request :get (expand-url (page-names stream-name))) 3000))]
    (siphon ch that-stream)
    (Thread/sleep 3000)))

(defn request-page-for-user
  ([page-name] (request-page-for-user page-name nil))
  ([page-name format]
     (condp = page-name
       "subscriptions"
       (fetch-page :get
                   (str "/users/" (:_id (get-this :user)) "/subscriptions"
                        (when format
                          (str "." (string/lower-case format)))))
       "user-meta"
       (fetch-page :get
                   (str "/main/xrd?uri=" (model.user/get-uri (get-this :user)))))))

(defn request-user-meta
  []
  (fetch-page :get
              (str "/main/xrd?uri=" (model.user/get-uri (get-this :user)))))

(defn response-should-be-redirect
  []
  (check-response
   (:status @current-page) => #(<= 300 %)
   (:status @current-page) => #(> 400 %)))

(defn response-should-be-sucsessful
  []
  (check-response
   (:status @current-page) => 200))

(defn should-be-admin
  []
  (check-response
   (session/current-user) => (contains {:admin true})))

(defn should-be-at-page
  [page-name]
  (check-response
   (let [path (get page-names page-name)]
     (current-url) => (re-pattern
                       (str ".*" (expand-url path)
                            ".*")))))

(defn should-be-logged-in
  []
  (check-response
   (exists? ".avatar") => truthy))

(defn should-have-content-type
  [type]
  (check-response
   (get-in @current-page [:headers "content-type"]) => type))

(defn should-get-a-document-of-type
  [type]
  (condp = type
    "as" (should-have-content-type "application/json")
    "JSON" (should-have-content-type "application/json")))

(defn should-have-field
  [field-name]
  (check-response
   (exists? (str "*[name='" field-name "']")) => truthy))

(defn should-not-be-logged-in
  []
  (check-response
   (exists? ".unauthenticated") => truthy))

(defn should-not-see-class
  [class-name]
  (check-response
   (exists? (str "." class-name)) =not=> truthy))

(defn should-receive-activity
  []
  (check-response
   (:displayName
    (:object
     (json/read-json
      (channel-buffer->string
       @(read-channel* that-stream
                       :timeout 60000))))) => (:title (get-this :activity))))

(defn should-receive-oembed
  []
  (check-response
   (page-source) => (re-pattern (:title (get-this :activity)))))

(defn should-see-activity
  []
  (check-response
   (exists? (format "article[data-id='%s']" (:_id (get-this :activity)))) => truthy))

(defn should-see-a-activity
  []
  (check-response
   (exists? ".activities") => truthy))

(defn should-see-n-users
  [n]
  (check-response
   (let [users (find-elements {:data-model "user"} #_"*[data-model='user']")]
     (count users) => n)))

(defn should-see-domain
  []
  (check-response
   (text ".domain-id") => (:_id (get-this :domain))))

(defn should-see-subscription
  []
  (if-let [subscription (get-this :subscription)]
    (let [elements (elements {:data-model "subscription"})]
      (check-response
       (map #(webdriver/attribute % :data-id) elements) => (contains (str (:_id subscription)))))
    (throw+ "could not find 'this' subscription")))

(defn should-see-domain-named
  [domain-name]
  (check-response
   (exists? (str "a[href='/main/domains/" domain-name "']")) => truthy))

(defn should-see-form
  []
  (check-response
   (exists? "form" ) => truthy))

(defn should-see-list
  [class-name]
  (check-response
   (exists? (str "." class-name)) => truthy))

(defn should-see-flash-message
  [message]
  (check-response
   (page-source) => (re-pattern message)))

(defn should-see-this
  [type]
  (if-let [record (get-this type)]
    (check-response
     (exists? (format "*[data-id='%s']"
                      ;; [data-model='%s']
                      (str (:_id record))
                      ;; (name type)
                      )) => truthy)
    (throw+ (format "Could not find 'this' for %s" type)))
  )

(defn should-not-see-button-for-that-user
  [button-name]
  (if-let [user (get-that :user)]
    (check-response
     (try (find-element-under (format "*[data-id='%s']" (str (:_id user)))
                              (webdriver/by-class-name (str button-name "-button")))
          (catch NoSuchElementException ex nil)) => falsey)
    (throw+ "no 'that' user")))

(defn should-see-subscription-list
  []
  (check-response
   (get-body) => #".*subscriptions"))

(defn subscription-should-be-deleted
  []
  (check-response
   (model.subscription/fetch-by-id (:_id (get-this :subscription))) => falsey))

(defn that-type-should-be-deleted
  [type]
  (if-let [record (get-that type)]
    (check-response
     (let [ns-str (str "jiksnu.model." (name type) "/fetch-by-id")
           find-fn (resolve (symbol ns-str))]
       (try (find-fn (:_id record))
            (catch RuntimeException ex nil)) => falsey))
    (throw+ (format "Could not find 'that' record for %s" type))))

(defn this-type-should-be-deleted
  [type]
  (if-let [record (get-this type)]
    (check-response
     (let [ns-str (str "jiksnu.model." (name type) "/fetch-by-id")
           find-fn (resolve (symbol ns-str))]
       (try (find-fn (:_id record))
            (catch RuntimeException ex nil)) => falsey))
    (throw+ (format "Could not find 'this' record for %s" type))))

(defn there-is-an-activity
  [& [options]]
  (let [modifier (:modifier options "public")
        user (or (:user options) (get-this :user) (a-user-exists))]
    (let [source (or (:feed-source options)
                     (get-this :feed-source)
                     (a-feed-source-exists))]
      (let [activity (session/with-user user
                       (actions.activity/create
                        (factory :activity
                                 {:author (:_id user)
                                  :update-source (:_id source)
                                  ;; :local true
                                  :public (= modifier "public")})))]
        (set-this :activity activity)
        activity))))

(defn there-is-an-activity-by-another
  [modifier]
  (let [user (actions.user/create (factory :local-user))]
    (there-is-an-activity {:modifier  modifier
                           :user user})))



(defn this-user-has-a-subscription
  []
  (let [subscription (model.subscription/create (factory :subscription {:actor (:_id (get-this :user))}))]
    (set-this :subscription subscription)))

(defn user-has-a-subscription
  []
  (let [subscription (model.subscription/create (factory :subscription {:actor (:_id (get-this :user))}))]
    (set-this :subscription subscription)))

(defn user-posts-activity
  []
  (there-is-an-activity {:modifier "public"}))

(defn that-user-posts-activity
  []
  (there-is-an-activity {:modifier "public"
                         :user (get-that :user)}))
