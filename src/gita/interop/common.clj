(ns gita.interop.common)

(defprotocol IData
  (-to-data [obj]))

(extend-protocol IData
  nil
  (-to-data [obj] obj)
  Object
  (-to-data [obj] obj))

(defmulti -meta-object (fn [type] type))

(defmethod -meta-object :default
  [type]
  {:class type
   :types #{}})

(defmulti -from-data (fn [obj type] type))
