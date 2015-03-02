(ns gita.interop.enum
  (:require [hara.reflect :as reflect]
            [hara.class.inheritance :as inheritance]))

(defn enum? [type]
  (if (-> (inheritance/ancestor-list type)
          (set)
          (get java.lang.Enum))
    true false))

(defn enum-values [type]
  (let [vf (reflect/query-class type ["$VALUES" :#])]
    (->> (vf type) (seq))))

(defn from-data
  [^Enum data ^Class type]
  (if-let [field (reflect/query-class type [data :#])]
    (field type)
    (throw (Exception. (str "Options for " (.getName type) " are: "
                            (mapv str (enum-values type)))))))

(defn to-data
  [^Enum enum] (str enum))

(def meta-object
  {:class java.lang.Enum
   :types #{String}
   :to-data to-data
   :from-data from-data})
