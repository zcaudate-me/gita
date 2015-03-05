(ns gita.api.apply
  (:require [gita.interop :as interop]))

(defn param-arg-match
  [^Class param-type ^Class arg-type]
  (cond (nil? arg-type)
        (-> param-type .isPrimitive not)

        (or (= param-type arg-type)
            (-> param-type (.isAssignableFrom arg-type)))
        true

        :else
        (condp = param-type
          Integer/TYPE (or (= arg-type Integer)
                           (= arg-type Long)
                           (= arg-type Long/TYPE)
                           (= arg-type Short/TYPE)
                           (= arg-type Byte/TYPE))
          Float/TYPE   (or (= arg-type Float)
                           (= arg-type Double/TYPE))
          Double/TYPE  (or (= arg-type Double)
                           (= arg-type Float/TYPE))
          Long/TYPE    (or (= arg-type Long)
                           (= arg-type Integer/TYPE)
                           (= arg-type Short/TYPE)
                           (= arg-type Byte/TYPE))
          Character/TYPE   (= arg-type Character)
          Short/TYPE       (= arg-type Short)
          Byte/TYPE        (= arg-type Byte)
          Boolean/TYPE     (= arg-type Boolean)
          false)))

(defn may-coerce [param arg]
  (let [targ (type arg)
        {:keys [types from-data]} (interop/meta-object param)]
    (cond (param-arg-match param targ) arg

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
