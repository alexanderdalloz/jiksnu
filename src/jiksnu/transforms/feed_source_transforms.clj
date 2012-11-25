(ns jiksnu.transforms.feed-source-transforms
  (:use [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model :as model])
  (:import java.net.URI))

(defn set-resource
  [item]
  (if (:resource item)
    item
    (let [resource (model/get-resource (:topic item))]
      (assoc item :resource (:_id resource)))))

(defn set-domain
  [source]
  (if (:domain source)
    source
    (let [uri (URI. (:topic source))
          domain-name (.getHost uri)
          domain (actions.domain/get-discovered
                  (actions.domain/find-or-create {:_id domain-name}))]
      (assoc source :domain (:_id domain)))))

(defn set-status
  [item]
  (if (:status item)
    item
    (assoc item :status "none")))

(defn set-local
  [item]
  (if (:local item)
    item
    (if-let [domain (actions.domain/find-or-create {:_id (:domain item)})]
      (assoc item :local (:local domain))
      (throw+ "Could not determine domain"))))
