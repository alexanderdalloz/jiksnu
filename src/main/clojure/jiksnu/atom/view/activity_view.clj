(ns jiksnu.atom.view.activity-view
  (:use jiksnu.model
        jiksnu.atom.view
        jiksnu.http.view
        jiksnu.view
        clojure.contrib.logging
        ciste.view
        [karras.entity :only (make)]
        jiksnu.namespace)
  (:require [jiksnu.model.user :as model.user]
            [jiksnu.atom.view.user-view :as view.user])
  (:import jiksnu.model.Activity
           org.apache.abdera.model.Entry
           org.apache.abdera.model.Element
           org.apache.abdera.ext.json.JSONUtil
           com.cliqset.abdera.ext.activity.object.Person
           java.io.StringWriter
           javax.xml.namespace.QName
           jiksnu.model.User))

(defn has-author?
  [^Entry entry]
  (not (nil? (.getAuthor entry))))

(defn parse-json-element
  "Takes a json object representing an Abdera element and converts it to
an Element"
  ([activity]
     (parse-json-element activity ""))
  ([{children :children
     attributes :attributes
     element-name :name
     :as activity} bound-ns]
     (let [xmlns (or (:xmlns attributes) bound-ns)]
       (let [qname (QName. xmlns element-name)
             element (.newExtensionElement *abdera-factory* qname)
             filtered (filter not-namespace attributes)]
         (doseq [[k v] filtered]
           (.setAttributeValue element (name k) v))
         (doseq [child children]
           (if (map? child)
             (.addExtension element (parse-json-element child xmlns))
             (if (string? child)
               (.setText element child))))
         element))))

(defn ^Entry new-entry
  [& opts]
  (let [entry (.newEntry *abdera*)]
    entry))

(defn add-extensions
  [^Entry entry ^Activity activity]
  (doseq [extension (:extensions activity)]
    (.addExtension entry (parse-json-element extension))))

(defn make-object
  [^Element element]
  (com.cliqset.abdera.ext.activity.Object. element))

(defn add-author
  [^Entry entry author-id]
  (if-let [user (model.user/fetch-by-id author-id)]
    (let [author-element (.addExtension entry atom-ns "author" "")
          author (Person. (make-object author-element))
          author-uri (full-uri user)
          author-name (:name user)
          author-jid  (str (:username user) "@" (:domain user))
          actor-element (.addExtension entry as-ns "actor" "activity")]
      (doto author
        (.setObjectType person-uri)
        (.setId (str "acct:" (:username user)
                     "@" (:domain user)))
        (.setName (:first-name user) (:last-name user))
        (.setDisplayName (:name user))
        (.addSimpleExtension atom-ns "email" ""
                             (or (:email user) author-jid))
        (.addSimpleExtension atom-ns "name" "" (:name user))
        (.addAvatar (:avatar-url user) "image/jpeg")
        (.addSimpleExtension atom-ns "uri" "" author-uri))
      (doto actor-element
        (.addSimpleExtension atom-ns "name" "" author-name)
        (.addSimpleExtension atom-ns "email" "" author-jid)
        (.addSimpleExtension atom-ns "uri" "" author-jid))
      (.addExtension entry author)
      (.addExtension entry actor-element))))

(defn add-authors
  [^Entry entry ^Activity activity]
  (dorun
   (map (partial add-author entry)
        (:authors activity)))
  entry)

(defn to-json
  "Serializes an Abdera entry to a json StringWriter"
  [^Entry entry]
  (let [string-writer (StringWriter.)]
    (JSONUtil/toJson entry string-writer)
    string-writer))

(defn parse-extension-element
  [element]
  (let [qname (.getQName element)
        name (.getLocalPart qname)
        namespace (.getNamespaceURI qname)]
    (if (and (= name "actor")
             (= namespace as-ns))
      (let [uri (.getSimpleExtension element atom-ns "uri" "")]
        {:authors [(:_id (model.user/find-or-create-by-uri uri))]})
      (if (and (= name "object")
                 (= namespace as-ns))
        (let [object (make-object element)]
          {:type (str (.getObjectType object))
           :object-id (str (.getId object))
           :object-updated (.getUpdated object)
           :object-published (.getPublished object)
           :object-content (.getContent object)})))))

(defn ^Activity to-activity
  "Converts an Abdera entry to the clojure representation of the json
serialization"
  [^Entry entry]
  (let [id (str (.getId entry))
        title (.getTitle entry)
        published (.getPublished entry)
        updated (.getUpdated entry)
        authors (.getAuthors entry)]
    (doall
     (map
      (fn [author]
        (println "author: " author))
      authors))
    (let [extension-maps
          (doall
           (map
            parse-extension-element
            (.getExtensions entry)))]
      (make Activity (apply merge
                            {:_id id
                             :published published
                             :updated updated
                             :title title}
                            extension-maps)))))

(defn ^Entry to-entry
  "Takes a json object that matches the results of serializing an Abdera
entry and converts it to an entry"
  [^Activity activity]
  (let [entry (new-entry)]
    (doto entry
      (.setId (:_id activity))
      (.setPublished (:published activity))
      (.setUpdated (:updated activity))
      (.setTitle (or (:title activity) (:summary activity)))
      (add-authors activity)
      (.addLink (full-uri activity) "alternate")
      (.setContentAsHtml (:summary activity))
      (.addSimpleExtension as-ns "object-type" "activity" status-uri)
      (.addSimpleExtension as-ns "verb" "activity" post-uri)
      (add-extensions activity))
    (if (:public activity)
      (let [rule-element (.addExtension entry osw-uri "acl-rule" "")]
        (let [action-element
              (.addSimpleExtension rule-element osw-uri
                                   "acl-action" "" view-uri)]
          (.setAttributeValue action-element "permission" grant-uri))
        (let [subject-element
              (.addExtension rule-element osw-uri "acl-subject" "")]
          (.setAttributeValue subject-element "type" everyone-uri))))


    (let [object-element (.addExtension entry as-ns "object" "activity")]
      (.setObjectType object-element status-uri)
      (.setUpdated object-element (:object-updated activity))
      (.setPublished object-element (:object-published activity))
      (.setId object-element (:object-id activity))
      (.setContentAsHtml object-element (:summary activity)))
    entry))
