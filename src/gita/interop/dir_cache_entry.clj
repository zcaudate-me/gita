(ns gita.interop.dir-cache-entry
  (:require [hara.reflect :as reflect]
            [gita.interop.common :as common]
            [gita.interop.util :as util])
  (:import org.eclipse.jgit.dircache.DirCacheEntry))

(defmethod common/-meta-object DirCacheEntry
  [type]
  {:class   DirCacheEntry
   :types   #{java.util.Map}
   :to-data common/-to-data})

(extend-protocol common/IData
  DirCacheEntry
  (-to-data [entry]
    (-> entry
        (util/object-data common/-to-data)
        (dissoc :raw-mode :raw-path))))

(defmethod print-method DirCacheEntry
  [v ^java.io.Writer w]
  (.write w (str "#e::" (common/-to-data v))))
