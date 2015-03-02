(ns gita.interop.dir-cache-entry
  (:import org.eclipse.jgit.dircache.DirCacheEntry))

(defn to-data [entry])

(def meta-object
  {:class   DirCacheEntry
   :types   #{java.util.Map}
   :to-data to-data})
