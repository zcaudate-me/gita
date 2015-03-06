(ns gita.interop.object-id
  (:require [gita.interop.common :as common])
  (:import [org.eclipse.jgit.lib AnyObjectId ObjectId]))

(defmethod common/-meta-object AnyObjectId
  [type]
  {:class     AnyObjectId
   :types     #{String}
   :to-data   common/-to-data
   :from-data common/-from-data})

(extend-protocol common/IData
  AnyObjectId
  (-to-data [id] (.getName id)))

(defmethod common/-from-data AnyObjectId
  [data _]
  (ObjectId/fromString data))

(defmethod print-method AnyObjectId
  [v ^java.io.Writer w]
  (.write w (str "#id::" (common/-to-data v))))
