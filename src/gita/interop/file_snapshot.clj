(ns gita.interop.file-snapshot
  (:require [hara.reflect :as reflect]
            [gita.interop.common :as common]
            [gita.interop.util :as util])
  (:import org.eclipse.jgit.internal.storage.file.FileSnapshot))

(defmethod common/-meta-object FileSnapshot
  [type]
  {:class     FileSnapshot
   :types     #{java.util.Map}
   :to-data   common/-to-data})

(extend-protocol common/IData
  FileSnapshot
  (-to-data [ss]
    (->> (reflect/delegate ss)
         (into {})
         (reduce-kv (fn [m k v]
                      (assoc m (-> k name util/java->clojure keyword) v))
                    {}))))

(defmethod print-method FileSnapshot
  [v ^java.io.Writer w]
  (.write w (str "#snapshot::" (common/-to-data v))))
