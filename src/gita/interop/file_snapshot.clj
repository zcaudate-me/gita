(ns gita.interop.file-snapshot
  (:require [hara.reflect :as reflect]
            [hara.object :as object]
            [hara.protocol.data :as data])
  (:import org.eclipse.jgit.internal.storage.file.FileSnapshot))

(defmethod data/-meta-object FileSnapshot
  [type]
  {:class     FileSnapshot
   :types     #{java.util.Map}
   :to-data   data/-to-data})

(extend-protocol data/IData
  FileSnapshot
  (-to-data [ss]
    (->> (reflect/delegate ss)
         (into {})
         (reduce-kv (fn [m k v]
                      (assoc m (-> k name object/java->clojure keyword) v))
                    {}))))

(defmethod print-method FileSnapshot
  [v ^java.io.Writer w]
  (.write w (str "#snapshot::" (data/-to-data v))))
