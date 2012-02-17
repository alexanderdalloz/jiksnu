(ns jiksnu.actions.activity-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]
               [debug :only [spy]])
        ciste.sections.default
        (clojure.core [incubator :only [-?> -?>>]]))
  (:require (aleph [http :as http])
            (clj-tigase [core :as tigase]
                        [element :as element])
            (clojure [string :as string])
            (clojure.java [io :as io])
            (clojure.tools [logging :as log])
            (hiccup [core :as hiccup])
            (jiksnu [abdera :as abdera]
                    [model :as model]
                    [namespace :as namespace]
                    [session :as session])
            (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [activity :as model.activity]
                          [domain :as model.domain]
                          [user :as model.user])
            (lamina [core :as l]))
  (:import javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera2.ext.thread.ThreadHelper
           org.apache.abdera2.model.Element))

(def ^QName activity-object-type (QName. namespace/as "object-type"))

(defn get-author
  [activity]
  (model.user/fetch-by-id (:author activity)))

(defn parse-reply-to
  [element]
  (let [parent-id (.getAttributeValue element "ref")]
    {:parent parent-id}))

(defn parse-geo
  [element]
  (let [coords (.getText element)
        [lat long] (string/split coords #" ")]
    {:lat lat :long long}))

(defn parse-notice-info
  [^Element element]
  (let [source (.getAttributeValue element "source")
        local-id (.getAttributeValue element "local_id")]
    {:source source
     :local-id local-id}))

(defn parse-irt
  [irt]
  (->> irt .getHref str))

(defn parse-irts
  [entry]
  (->> (ThreadHelper/getInReplyTos entry)
       (map parse-irt)
       (filter identity)))

(defn parse-link
  [link]
  (if-let [href (str (.getHref link))]
    (when (and (re-find #"^.+@.+$" href)
               (not (re-find #"node=" href)))
      href)))

(defn parse-extension-element
  [element]
  (let [qname (.getQName element)
        qname (element/parse-qname qname)]
    (condp = (:namespace qname)
      namespace/as (condp = (:name qname)
                     "actor" nil
                     "object" (abdera/parse-object-element element)
                     nil)

      namespace/statusnet (condp = (:name qname)
                            "notice_info" (parse-notice-info element)  
                            nil)

      namespace/thr (condp = (:name qname)
                      "in-reply-to" (parse-reply-to element)
                      nil)

      namespace/geo (condp = (:name qname)
                      "point" (parse-geo element)
                      nil)

      nil)))

(defn set-recipients
  [activity]
  (let [recipients (filter identity (:recipients activity))]
    (if (not (empty? recipients))
      (let [users (map actions.user/user-for-uri recipients)]
        (assoc activity :recipients users))
      (dissoc activity :recipients))))

(defaction create
  [params]
  (model.activity/create params))

(defaction delete
  "delete it"
  [activity]
  (let [actor-id (session/current-user-id)
        author (:author activity)]
    (if (or (session/is-admin?) (= actor-id author))
      (model.activity/delete activity))))

(defaction edit-page
  [id]
  (model.activity/fetch-by-id id))

(defn ^Activity entry->activity
  "Converts an Abdera entry to the clojure representation of the json
serialization"
  ([entry] (entry->activity entry nil))
  ([entry feed]
     (let [id (str (.getId entry))
           original-activity (model.activity/fetch-by-remote-id id)
           title (.getTitle entry)
           published (.getPublished entry)
           updated (.getUpdated entry)
           verb (-?> entry
                     (.getExtension (QName. namespace/as "verb" "activity"))
                     .getText
                     model/strip-namespaces)
           user (-> entry
                    (abdera/get-author feed)
                    actions.user/person->user
                    actions.user/find-or-create-by-remote-id)
           extension-maps (->> (.getExtensions entry)
                               (map parse-extension-element)
                               doall)
           irts (parse-irts entry)
           content (.getContent entry)
           links (abdera/parse-links entry)
           mentioned-uri (-?> entry
                              (.getLink "mentioned")
                              .getHref str)
           conversation (-?> entry 
                             (.getLink "ostatus:conversation")
                             .getHref str)
           tags (filter (complement #{""}) (abdera/parse-tags entry))
           object-element (.getExtension entry (QName. namespace/as "object"))
           object-type (-?> (or (-?> object-element (.getFirstChild activity-object-type))
                                (-?> entry (.getExtension activity-object-type)))
                            .getText model/strip-namespaces)
           object-id (-?> object-element (.getFirstChild (QName. namespace/atom "id")))
           opts (apply merge
                       (when published        {:published published})
                       (when content          {:content content})
                       (when updated          {:updated updated})
                       ;; (when (seq recipients) {:recipients (string/join ", " recipients)})
                       (when title            {:title title})
                       (when (seq irts)       {:irts irts})
                       (when (seq links)      {:links links})
                       (when conversation     {:conversation conversation})
                       (when mentioned-uri    {:mentioned-uri mentioned-uri})
                       (when (seq tags)       {:tags tags})
                       (when verb             {:verb verb})
                       {:id id
                        :author (:_id user)
                        :public true
                        :object (merge (when object-type {:object-type object-type})
                                       (when object-id {:id object-id}))
                        :comment-count (abdera/get-comment-count entry)}
                       extension-maps)]
       (model.activity/make-activity opts))))

(defn get-activities
  [feed]
  (map #(entry->activity % feed)
       (.getEntries feed)))

;; TODO: merge this with h.a/load-activities
(defaction fetch-remote-feed
  [uri]
  (let [feed (abdera/fetch-feed uri)]
    (doseq [activity (get-activities feed)]
      (create activity))))

;; (defaction find-or-create
;;   [options]
;;   (model.activity/find-or-create options))

(defaction new
  [action request]
  (Activity.))

(defaction post
  [activity]
  ;; TODO: validate user
  (when-let [prepared-post (-> activity
                               model.activity/prepare-post
                               (dissoc :pictures))]
    (-> activity :pictures model.activity/parse-pictures)
    (create prepared-post)))

(defaction remote-create
  [activities]
  (doseq [activity activities]
    (create activity))
  true)

(defaction show
  [id]
  (model.activity/show id))

(defaction update
  [activity]
  (let [{{id :_id} :params} activity
        original-activity (model.activity/fetch-by-id id)
        opts
        (model.activity/make-activity
         (merge original-activity
                activity
                (when (= (get activity :public) "public")
                  {:public true})))]
    (model.activity/update (dissoc opts :picture))))

(definitializer
  (doseq [namespace ['jiksnu.filters.activity-filters
                     'jiksnu.sections.activity-sections
                     'jiksnu.triggers.activity-triggers
                     'jiksnu.views.activity-views]]
    (require namespace)))
