(ns gita.protocol)

(defprotocol IData
  (-to-data [obj])
  (-data-types [obj]))

(defmulti -from-data (fn [obj type] type))

(extend-protocol IData
  Object
  (-to-data [obj] obj))
