(ns gita.interop.dir-cache
  (:require [gita.interop.dir-cache-entry :as entry])
  (:import org.eclipse.jgit.dircache.DirCache))

(defn to-data [dir-cache])

(def meta-object
  {:class   DirCache
   :types   #{String}
   :to-data to-data})
