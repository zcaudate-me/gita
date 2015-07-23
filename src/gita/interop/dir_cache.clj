(ns gita.interop.dir-cache
  (:require [hara.protocol.data :as data]
            [hara.object :as object])
  (:import org.eclipse.jgit.dircache.DirCache))

(defmethod data/-meta-object DirCache
  [type]
  {:class   DirCache
   :types   #{String}
   :to-data data/-to-data})

(defn process-entry [entry]
  [(:path-string entry)
   (reduce-kv (fn [s k v]
                (if (= true v) (conj s (-> k name (.replace "?" "") keyword)) s))
              #{} entry)])

(extend-protocol data/IData
  DirCache
  (-to-data [^DirCache dir-cache]
    (let [count (.getEntryCount dir-cache)]
      (->> (map (fn [^Long i] (-> (.getEntry dir-cache i) object/to-data process-entry))
                (range count))
           (into {})))))

(defmethod print-method DirCache
  [v ^java.io.Writer w]
  (.write w (str "#dir " (data/-to-data v))))
