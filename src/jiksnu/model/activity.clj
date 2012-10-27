(ns jiksnu.model.activity
  (:use [ciste.config :only [config]]
        [clojure.core.incubator :only [-?>>]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of acceptance-of]])
  (:require [clj-statsd :as s]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [lamina.trace :as trace]
            [monger.collection :as mc]
            [monger.query :as mq])
  (:import jiksnu.model.Activity))

(defonce page-size 20)
(def collection-name "activities")

(def create-probe (trace/probe-channel :activity:created))

(def create-validators
  (validation-set
   (presence-of   :_id)
   (presence-of   :id)
   (presence-of   :title)
   (presence-of   :author)
   (presence-of   :content)
   (acceptance-of :local         :accept (partial instance? Boolean))
   (acceptance-of :public        :accept (partial instance? Boolean))
   (presence-of   :update-source)
   (presence-of   [:object :object-type])
   (presence-of   :verb)
   ;; (presence-of   :conversation)

   ;; TODO: These should be joda times
   (presence-of   :created)
   (presence-of   :updated)
   ))

(defn get-author
  [activity]
  (-> activity
      :author
      model.user/fetch-by-id))

(defn get-link
  [user rel content-type]
  (first (model/rel-filter rel (:links user) content-type)))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (let [sort-clause (mq/partial-query (mq/sort (:sort-clause options)))
           records (mq/with-collection collection-name
                     (mq/find params)
                     (merge sort-clause)
                     (mq/paginate :page (:page options 1)
                                  :per-page (:page-size options 20)))]
       (map model/map->Activity records))))

(defn fetch-by-id
  [id]
  ;; TODO: Should this always take a string?
  (let [id (if (string? id) (model/make-id id) id)]
    (if-let [activity (mc/find-map-by-id collection-name id)]
      (model/map->Activity activity))))

(defn create
  [params]
  (let [errors (create-validators params)]
    (if (empty? errors)
      (do
        (log/debugf "Creating activity: %s" (pr-str params))
        (mc/insert collection-name params)
        (let [item (fetch-by-id (:_id params))]
          (trace/trace :activity:created item)
          item))
      (throw+ {:type :validation :errors errors}))))

(defn get-comments
  [activity]
  (fetch-all {:parent (:_id activity)}
             {:sort [{:created 1}]}))

(defn author?
  [activity user]
  (= (:author activity) (:_id user)))

(defn update
  [activity]
  (mc/save collection-name activity))

(defn privacy-filter
  [user]
  (if user
    (if (not (session/is-admin? user))
      {:$or [{:public true}
             {:author (:_id user)}]})
    {:public true}))

(defn fetch-by-remote-id
  [id]
  (if-let [activity (mc/find-one-as-map collection-name {:id id})]
    (model/map->Activity activity)))

(defn drop!
  []
  (mc/remove collection-name))

(defn delete
  [activity]
  (mc/remove-by-id collection-name (:_id activity))
  activity)

(defn add-comment
  [parent comment]
  (mc/update collection-name
             {:_id (:_id parent)}
             {:$push {:comments (:_id comment)}}))

(defn parse-pictures
  [picture]
  (let [filename (:filename picture)
        tempfile (:tempfile picture)
        user-id (str (session/current-user-id))
        dest-file (io/file (str user-id "/" filename))]
    (when (and (not= filename "") tempfile)
      (.mkdirs (io/file user-id))
      (io/copy tempfile dest-file))))

(defn count-records
  ([] (count-records {}))
  ([params]
     (mc/count collection-name params)))
