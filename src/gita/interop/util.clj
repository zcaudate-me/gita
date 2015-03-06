(ns gita.interop.util
  (:require [hara.string.case :as case]
            [hara.reflect :as reflect]))

(defn java->clojure [name]
  (let [nname (cond (re-find #"^get.+" name)
                    (subs name 3)

                    (re-find #"^is.+" name)
                    (str (subs name 2) "?")

                    (re-find #"^has.+" name)
                    (str (subs name 3) "?")

                    :else name)]
    (case/spear-case nname)))

(defn object-methods [obj]
  (let [t (type obj)]
    (reflect/query-class t [#"^(is)|(get)" [t]])))

(defn object-data
  ([obj] (object-data obj identity))
  ([obj f]
    (->> (object-methods obj)
         (reduce (fn [m ele]
                   (assoc m (-> ele :name java->clojure keyword)
                          (f (ele obj))))
                 {}))))
