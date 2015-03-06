(ns gita.interop.common)

(defprotocol IData
  (-to-data [obj]))

(defn to-data [obj]
  (cond (nil? obj) nil

        (.isArray (type obj))
        (->> (seq obj)
             (mapv to-data))

        :else
        (-to-data obj)))

(extend-protocol IData
  nil
  (-to-data [obj] obj)
  Object
  (-to-data [obj] obj)

  java.util.Iterator
  (-to-data [obj] (->> obj iterator-seq (mapv to-data))))

(defmulti -meta-object (fn [type] type))

(defmethod -meta-object :default
  [type]
  {:class type
   :types #{}})

(defmulti -from-data (fn [obj type] type))
