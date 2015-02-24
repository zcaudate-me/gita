(ns gita.interop.status
  (:require [hara.reflect :as reflect]
            [gita.protocol :as protocol]
            [gita.interop.util :as util])
  (:import org.eclipse.jgit.api.Status))

(extend-protocol protocol/IData
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
                   {}))))
  (-data-types [_] #{java.util.Map}))
