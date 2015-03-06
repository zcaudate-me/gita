(ns gita.interop.enum
  (:require [gita.interop.common :as common]
            [hara.reflect :as reflect]
            [hara.class.inheritance :as inheritance]))

(defn enum? [type]
  (if (-> (inheritance/ancestor-list type)
          (set)
          (get java.lang.Enum))
    true false))

(defn enum-values [type]
  (let [vf (reflect/query-class type ["$VALUES" :#])]
    (->> (vf type) (seq))))

(defmethod common/-meta-object Enum
  [type]
  {:class java.lang.Enum
   :types #{String}
   :to-data common/-to-data
   :from-data common/-from-data})

(extend-protocol common/IData
  Enum
  (-to-data
    [enum] (str enum)))

(defmethod common/-from-data Enum
  [data type]
  (if-let [field (reflect/query-class type [data :#])]
    (field type)
    (throw (Exception. (str "Options for " (.getName type) " are: "
                            (mapv str (enum-values type)))))))
