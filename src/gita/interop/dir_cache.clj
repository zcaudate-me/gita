(ns gita.interop.dir-cache
  (:require [hara.object :as object])
  (:import [org.eclipse.jgit.dircache
            DirCache DirCacheEntry]))

(defn process-entry [entry]
  [(:path-string entry)
   (reduce-kv (fn [s k v]
                (if (= true v) (conj s (-> k name (.replace "?" "") keyword)) s))
              #{} entry)])

(object/map-like
  DirCache
  {:tag "dir"
   :read {:to-map
          (fn [^DirCache dir-cache]
            (let [count (.getEntryCount dir-cache)]
              (->> (map (fn [^Long i] (-> (.getEntry dir-cache i) object/to-data process-entry))
                        (range count))
                   (into {}))))}}

  DirCacheEntry
  {:tag "e" :exclude [:raw-mode :raw-path]})
