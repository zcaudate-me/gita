(ns gita.interop.enum
  (:require [hara.reflect :as reflect]
            [hara.class.inheritance :as inheritance]
            [gita.protocol :as protocol]))

(defn enum? [type]
  (if (-> (inheritance/ancestor-list type)
          (set)
          (get java.lang.Enum))
    true false))

(defn enum-values [type]
  (let [vf (reflect/query-class type ["$VALUES" :#])]
    (->> (vf type) (seq))))

(defmethod protocol/-from-data
  java.lang.Enum
  [data type]
  (if-let [field (reflect/query-class type [data :#])]
    (field type)
    (throw (Exception. (str "Options for " (.getName type) " are: "
                            (mapv str (enum-values type)))))))

(extend-protocol protocol/IData
  java.lang.Enum
  (-to-data [enum] (str enum))
  (-data-types [_] #{String}))
