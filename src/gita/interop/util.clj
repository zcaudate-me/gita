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

(defn object-methods
  ([obj]
   (if obj
     (->> (reflect/query-instance obj [#"^(is)|(get)" 1 :instance])
          (reduce (fn [m ele]
                    (assoc m (-> ele :name java->clojure keyword) ele))
                  {}))
     {})))

(defn object-apply [methods obj f]
  (reduce-kv (fn [m k ele]
               (try
                 (let [res (f (ele obj))]
                   (assoc m k res))
                 (catch Throwable t
                   (println "Cannot process:" ele f)
                   m)))
             {} methods))

(defn object-data
  ([obj] (object-data obj identity))
  ([obj f]
    (-> (object-methods obj)
        (object-apply obj f))))
