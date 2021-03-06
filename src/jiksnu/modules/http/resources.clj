(ns jiksnu.modules.http.resources
  (:require [octohipster.core :as octo]
            [octohipster.routes :as octo-routes]
            [taoensso.timbre :as timbre]))

(defonce groups
  ;; "Ref holding each api group"
  (ref []))

(defonce resources (ref {}))

(defn get-groups
  [site-var]
  (let [m (meta site-var)]
    (var-get (ns-resolve (:ns m) (symbol (str (:name m) "-groups"))))))

(defn add-group!
  [site-var group-var]
  #_(timbre/debugf "adding group %s %s" site-var group-var)
  (dosync
   (alter (get-groups site-var) conj group-var)))

;; TODO: implement
(defn get-resource
  [group-var resource-name])

(defn get-resources
  [group-var]
  (let [m (meta group-var)]
    (var-get (ns-resolve (:ns m) (symbol (str (:name m) "-resources"))))))

(defn add-resource!
  [group-var resource-name resource]
  ;; (timbre/debugf "adding resource %s(%s)" (:name (meta group-var)) resource-name)
  (dosync
   (alter (get-resources group-var) assoc resource-name resource)))

(defn get-route
  [site-var]
  (let [m (meta site-var)]
    (var-get (ns-resolve (:ns m) (symbol (str (:name m) "-routes"))))))

(defn init-site-reloading!
  "Initialize reloading for the var site"
  [site]
  (let [{site-ns :ns site-name :name} (meta site)
        groups-var (ns-resolve site-ns (symbol (str site-name "-groups")))
        groups @(var-get groups-var)
        init-var (ns-resolve site-ns (symbol (str site-name "-init")))]
    (doseq [group-var groups]
      (let [{group-ns :ns group-name :name} (meta group-var)
            resources-var (ns-resolve group-ns (symbol (str group-name "-resources")))
            resources (var-get resources-var)]
        (add-watch resources :site (fn [k r os ns] (timbre/debug "refreshing site") (init-var)))))))

(defn update-groups
  [groups]
  (map
   (fn [gvar]
     #_(timbre/debug (str "Processing Group: " gvar))
     (let [options (var-get gvar)
           group-resources (map val @(get-resources gvar))
           options (assoc options :resources group-resources)]
       (octo/group options)))
   groups))

(defmacro defresource
  [group resource-name & {:as options}]
  #_(timbre/debugf "defining resource: %s(%s)" group resource-name)
  `(add-resource! (var ~group) ~resource-name (octo/resource ~(assoc options :name resource-name))))

(defmacro defgroup
  [site-sym group-sym & {:as opts}]
  (let [resources-sym (symbol (str group-sym "-resources"))]
    `(do
       (def ~group-sym ~opts)
       (defonce ~resources-sym (ref {}))
       (add-group! (var ~site-sym) (var ~group-sym)))))

(defmacro defsite
  [site-name & {:as opts}]
  (let [route-sym    (symbol (str site-name "-routes"))
        resource-sym (symbol (str site-name "-resources"))
        group-sym    (symbol (str site-name "-groups"))
        init-sym     (symbol (str site-name "-init"))
        options (merge {:name (str site-name)
                        :groups @groups}
                       opts)]
    `(do
       (def ~site-name ~options)
       (declare ~route-sym)
       (declare ~resource-sym)
       (defonce ~group-sym (ref #{}))
       (def ~init-sym (fn []
                        (let [groups# (update-groups @~group-sym)
                              body# (assoc ~options :groups groups#)]
                          ;; (timbre/debugf "Creating Site. Groups %s" (count groups#))
                          (let [routes# (octo-routes/routes body#)]
                            (alter-var-root (var ~route-sym) (fn [_#] routes#))))))
       (~init-sym))))
