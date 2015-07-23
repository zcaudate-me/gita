(ns gita.interop.status
  (:require [hara.reflect :as reflect]
            [hara.protocol.data :as data]
            [hara.object :as object])
  (:import org.eclipse.jgit.api.Status))

(defmethod data/-meta-object Status
  [type]
  {:class     Status
   :types     #{java.util.Map}
   :to-data   data/-to-data})

(extend-protocol data/IData
  Status
  (-to-data [status]
    (let [methods (reflect/query-class status [:method])]
      (->> methods
           (map (fn [ele] [(-> ele :name object/java->clojure keyword)
                          (ele status)]))
           (reduce (fn [m [k v]]
                     (if (and (or (instance? java.util.Collection v)
                                  (instance? java.util.Map v))
                              (empty? v))
                       m
                       (assoc m k v)))
                   {})))))

(defmethod print-method Status
  [v ^java.io.Writer w]
  (.write w (str "#status::" (data/-to-data v))))
