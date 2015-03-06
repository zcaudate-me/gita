(ns gita.interop.dir-cache
  (:require [gita.interop.common :as common])
  (:import org.eclipse.jgit.dircache.DirCache))

(defmethod common/-meta-object DirCache
  [type]
  {:class   DirCache
   :types   #{String}
   :to-data common/-to-data})

(defn process-entry [entry]
  [(:path-string entry)
   (reduce-kv (fn [s k v]
                (if (= true v) (conj s (-> k name (.replace "?" "") keyword)) s))
              #{} entry)])

(extend-protocol common/IData
  DirCache
  (-to-data [dir-cache]
    (let [count (.getEntryCount dir-cache)]
      (->> (map (fn [i] (-> (.getEntry dir-cache i) common/-to-data process-entry))
                (range count))
           (into {})))))

(defmethod print-method DirCache
  [v ^java.io.Writer w]
  (.write w (str "#dir::" (common/-to-data v))))
