(ns gita.interop.status
  (:require [hara.reflect :as reflect]
            [gita.interop.util :as util])
  (:import org.eclipse.jgit.api.Status))

(defn to-data [status]
  (let [methods (reflect/query-class status [:method])]
    (->> methods
         (map (fn [ele] [(-> ele :name util/java->clojure keyword)
                        (ele status)]))
         (reduce (fn [m [k v]]
                   (if (and (or (instance? java.util.Collection v)
                                (instance? java.util.Map v))
                            (.isEmpty v))
                     m
                     (assoc m k v)))
                 {}))))

(def meta-object
  {:class     Status
   :types     #{java.util.Map}
   :to-data   to-data})

(defmethod print-method Status
  [v ^java.io.Writer w]
  (.write w (str "#status::" (to-data v))))
