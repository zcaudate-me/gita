(ns gita.api.apply
  (:require [gita.interop :as interop]
            [hara.reflect.util :as util]))

(defn may-coerce [^Class param arg]
  (let [^Class targ (type arg)
        {:keys [types from-data]} (interop/meta-object param)]
    (cond (util/param-arg-match param targ) arg

          :else
          (if (and (-> types empty? not)
                   (-> from-data nil? not)
                   (types targ))
            (from-data arg param)
            (throw (Exception. (str "Cannot convert value " arg
                                    " of type " (.getName targ) " to " (.getName param)) ))))))

(defn apply-with-coercion
  ([{:keys [params] :as ele} args]
   (apply ele (map may-coerce params args))))
