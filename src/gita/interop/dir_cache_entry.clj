(ns gita.interop.dir-cache-entry
  (:require [hara.reflect :as reflect]
            [gita.interop.util :as util]
            [gita.interop.object-id :as object-id]
            [gita.interop.file-mode :as file-mode])
  (:import org.eclipse.jgit.dircache.DirCacheEntry))

(def entry-methods
  (reflect/query-class DirCacheEntry [#"^(is)|(get)" [DirCacheEntry]]))

(defn to-data [entry]
  (-> entry-methods
      (->> (reduce (fn [m ele]
                     (assoc m (-> ele :name util/java->clojure keyword) (ele entry)))
                   {}))
      (update-in [:object-id] object-id/to-data)
      (update-in [:file-mode] file-mode/to-data)
      (dissoc :raw-mode :raw-path)))

(def meta-object
  {:class   DirCacheEntry
   :types   #{java.util.Map}
   :to-data to-data})

(defmethod print-method DirCacheEntry
  [v ^java.io.Writer w]
  (.write w (str "#e::" (to-data v))))
