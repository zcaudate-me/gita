(ns gita.interop.object-id
  (:import [org.eclipse.jgit.lib AnyObjectId ObjectId]))

(defn to-data [id]
  (.getName id))

(defn from-data [data _]
  (ObjectId/fromString data))

(def meta-object
  {:class     ObjectId
   :types     #{String}
   :to-data   to-data
   :from-data from-data})

(defmethod print-method AnyObjectId
  [v ^java.io.Writer w]
  (.write w (str "#id::" (to-data v))))
