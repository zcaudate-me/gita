(ns gita.interop.object-id
  (:require [gita.protocol :as protocol])
  (:import [org.eclipse.jgit.lib AnyObjectId ObjectId]))

(extend-protocol protocol/IData
  AnyObjectId
  (-to-data [id] (.getName id))
  (-data-types [_] #{String}))

(defmethod protocol/-from-data AnyObjectId
  [^String data _]
  (ObjectId/fromString data))
