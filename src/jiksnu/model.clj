(ns jiksnu.model
  (:use ciste.core
        [ciste.config :only [config definitializer environment]]
        [clj-factory.core :only [factory]]
        [clojure.core.incubator :only [-?> -?>>]])
  (:require [ciste.model :as cm]
            [clojure.string :as string]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [inflections.core :as inf]
            [jiksnu.namespace :as ns]
            [lamina.core :as l]
            [monger.collection :as mc]
            [monger.core :as mg]
            monger.joda-time
            monger.json
            [plaza.rdf.core :as rdf]
            [plaza.rdf.implementations.jena :as jena])
  (:import com.ocpsoft.pretty.time.PrettyTime
           java.io.FileNotFoundException
           java.io.PrintWriter
           java.text.SimpleDateFormat
           java.util.Date
           java.net.URL
           lamina.core.channel.Channel
           org.bson.types.ObjectId
           org.joda.time.DateTime
           java.io.StringReader))

(def ^:dynamic *date-format* "yyyy-MM-dd'T'hh:mm:ss'Z'")

(def ^:dynamic *mongo-database* (ref nil))

;; TODO: pull these from ns/
(defonce bound-ns {:hm "http://host-meta.net/xrd/1.0"
                   :xrd "http://docs.oasis-open.org/ns/xri/xrd-1.0"})

(defn format-date
  "This is a dirty little function to get a properly formatted date."
  ;; TODO: Get more control of passed dates
  [^Date date]
  (condp = (class date)
    String (DateTime/parse date)
    DateTime date
    Date (let [formatter (SimpleDateFormat. *date-format*)]
           (.setTimeZone formatter (java.util.TimeZone/getTimeZone "UTC"))
           (.format formatter  date))
    date))

(defn date->twitter
  [date]
  (let [formatter (SimpleDateFormat. "EEE MMM d HH:mm:ss Z yyyy")]
    (.setTimeZone formatter (java.util.TimeZone/getTimeZone "UTC"))
    (.format formatter date)))

(defn prettyify-time
  [^Date date]
  (-?>> date (.format (PrettyTime.))))

(defn strip-namespaces
  [val]
  (-?> val
       (string/replace #"http://activitystrea.ms/schema/1.0/" "")
       (string/replace #"http://ostatus.org/schema/1.0/" "")))



(defn rel-filter
  "returns all the links in the collection where the rel value matches the
   supplied value"
  ([rel links] (rel-filter rel links nil))
  ([rel links content-type]
     (filter (fn [link]
               (and (= (:rel link) rel)
                    (or (not content-type) (= (:type link) content-type))))
             links)))

;; TODO: I'm sure this exists somewhere else
;; all this is really doing is assembling a uri
(defn make-subscribe-uri
  [url options]
  (str url "?"
       (string/join
        "&"
        (map
         (fn [[k v]] (str (name k) "=" v))
         options))))

(defn with-subject
  "Inserts the subject into the first position in the sequence of vectors"
  [s pairs]
  (map (fn [[p o]] [s p o]) pairs))

(jena/init-jena-framework)
;; TODO: Find a better ns for this
(rdf/register-rdf-ns :dc ns/dc)
(rdf/register-rdf-ns :foaf ns/foaf)
(rdf/register-rdf-ns :sioc ns/sioc)
(rdf/register-rdf-ns :cert ns/cert)
(rdf/register-rdf-ns :aair ns/aair)
(rdf/register-rdf-ns :as ns/as)

(defn force-coll
  [x]
  (if (coll? x)
    x (list x)))

(defrecord Activity                [])
(defrecord AuthenticationMechanism [])
(defrecord Conversation            [])
(defrecord Domain                  [])
(defrecord FeedSource              [])
(defrecord FeedSubscription        [])
(defrecord Group                   [])
(defrecord Item                    [])
(defrecord Key                     [])
(defrecord Like                    [])
(defrecord Subscription            [])
(defrecord User                    [])

;; Entity predicates

(defn activity?
  [activity]
  (instance? Activity activity))

(defn domain?
  [domain]
  (instance? Domain domain))

(defn subscription?
  [subscription]
  (instance? Subscription subscription))

(defn user?
  "Is the provided object a user?"
  [user] (instance? User user))

(defn drop-collection
  [klass]
  (mc/remove (inf/plural (inf/underscore (.getSimpleName klass)))))


(defmacro make-indexer
  [namespace-sym & options]
  `(do (require ~namespace-sym)
       (let [ns-ns# (the-ns ~namespace-sym)
             sort-clause# (get ~options :sort-clause [{:updated -1}])
             page-size# (get ~options :page-size 20)
             count-fn# (ns-resolve ns-ns# (symbol "count-records"))
             fetch-fn# (ns-resolve ns-ns# (symbol "fetch-all" ))]
         (fn [& [{:as params#} & [{:as options#} & _#]]]
           (let [options# (or options# {})
                 page# (get options# :page 1)
                 criteria# {:sort sort-clause#
                            :skip (* (dec page#) page-size#)
                            :limit page-size#}
                 record-count# (count-fn# params#)
                 records# (fetch-fn# params# criteria#)]
             {:items records#
              :page page#
              :page-size page-size#
              :total-records record-count#
              :args options#})))))


(defn drop-all!
  "Drop all collections"
  []
  (doseq [entity [Activity AuthenticationMechanism Domain
                  FeedSource FeedSubscription
                  Group Item Key Like Subscription User]]
    (drop-collection entity)))

(defn parse-http-link
  [url]
  (let [[_ href remainder] (re-matches #"<([^>]+)>; (.*)" url)]
    (->> (string/split remainder #"; ")
         (map #(let [[_ k v] (re-matches #"(.*)=\"(.*)\"" %)] [k v]))
         (into {})
         (merge {"href" href}))))

(defn ^ObjectId make-id
  "Create an object id from the provided string"
  ([] (ObjectId.))
  ([^String id] (ObjectId. id)))

;; this could be more generic
(defn extract-atom-link
  "Find the atom link in the page identified by url"
  [url]
  (->> url
       cm/get-links
       (filter #(= "alternate" (:rel (:attrs %))))
       (filter #(= "application/atom+xml" (:type (:attrs %))))
       (map #(-> % :attrs :href))
       first))

;; Database functions

(defn set-database!
  "Set the connection for mongo"
  []
  (log/info (str "setting database for " (environment)))
  ;; TODO: pass connection options
  (mg/connect!)
  (let [db (mg/get-db (str (config :database :name)))]
    (mg/set-db! db)))

;; TODO: Find a good place for this

(defn write-json-date
  ([^Date date ^PrintWriter out]
     (write-json-date date out false))
  ([^Date date ^PrintWriter out escape-unicode?]
     (let [formatted-date (.format (SimpleDateFormat. *date-format*) date)]
       (.print out (str "\"" formatted-date "\"")))))

(defn write-json-object-id
  ([id ^PrintWriter out]
     (write-json-object-id id out false))
  ([id ^PrintWriter out escape-unicode]
     (.print out (str "\"" id "\""))))

(extend Date json/Write-JSON
        {:write-json write-json-date})
(extend ObjectId json/Write-JSON
        {:write-json write-json-object-id})

(definitializer
  (set-database!))

