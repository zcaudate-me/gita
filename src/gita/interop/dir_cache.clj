(ns gita.interop.dir-cache
  (:require [gita.interop.dir-cache-entry :as entry])
  (:import org.eclipse.jgit.dircache.DirCache))

(defn process-entry [entry]
  [(:path-string entry)
   (reduce-kv (fn [s k v]
                (if (= true v) (conj s (-> k name (.replace "?" "") keyword)) s))
              #{} entry)])

(defn to-data [dir-cache]
  (let [count (.getEntryCount dir-cache)]
    (->> (map (fn [i] (-> (.getEntry dir-cache i) entry/to-data process-entry))
              (range count))
         (into {}))))

(def meta-object
  {:class   DirCache
   :types   #{String}
   :to-data to-data})

(defmethod print-method DirCache
  [v ^java.io.Writer w]
  (.write w (str "#dir::" (to-data v))))
