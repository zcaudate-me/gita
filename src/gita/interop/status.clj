(ns gita.interop.status
  (:require [hara.reflect :as reflect]
            [gita.interop.common :as common]
            [gita.interop.util :as util])
  (:import org.eclipse.jgit.api.Status))

(defmethod common/-meta-object Status
  [type]
  {:class     Status
   :types     #{java.util.Map}
   :to-data   common/-to-data})

(extend-protocol common/IData
  Status
  (-to-data [status]
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
                   {})))))

(defmethod print-method Status
  [v ^java.io.Writer w]
  (.write w (str "#status::" (common/-to-data v))))
