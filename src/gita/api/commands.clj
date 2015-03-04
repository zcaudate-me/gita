(ns gita.api.commands
  (:require [hara.reflect :as reflect]
            [hara.reflect.util :as util]
            [hara.common.string :refer [to-string]]
            [hara.string.case :as case]
            [clojure.string :as string]
            [gita.interop :as interop])
  (:import org.eclipse.jgit.api.Git))

(defn git-all-commands []
  (->> (reflect/query-class Git [:name :type])
       (map (fn [m] (assoc m :command (to-string (:type m)))))
       (filter (fn [m] (.endsWith (:command m) "Command")))
       (map (fn [m] (-> m :name (case/spear-case) (string/split #"-") (->> (map keyword)))))
       (reduce (fn [m [root sub]]
                 (if sub
                   (update-in m [root] (fnil conj #{}) sub)
                   (assoc-in m [root] #{})))
               {})))

(defn command-options [command]
  (->> (reflect/query-class command [:method (type command)])
       (map (fn [ele]
              (let [nm   (case/spear-case (:name ele))
                    op-type (if (= "set" (subs nm 0 3))
                              :single
                              :multi)
                    op-key  (keyword (if (re-find #"((add)|(set)).+" nm)
                                       (subs nm 4)
                                       nm))]
                [op-key {:type  op-type
                         :key   op-key
                         :element ele}])))
       (into {})))

(defn command-input [opt]
  (let [p (-> opt :element :params second)
        t (:type opt)]
    (case t
      :single p :multi [p])))

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

(defn command-initialize-inputs
  [command inputs]
  (let [options (command-options command)]
    (loop [[slug & more] inputs
           command command]
      (cond (nil? slug) command

            :else
            (if-let [field (get options slug)]
              (let [ele (:element field)
                    ptypes (:params ele)
                    pcount (dec (count ptypes))]
                (case (:type field)
                  :single (let [curr (take pcount more)
                                nxt  (drop pcount more)]
                            (recur nxt (apply-with-coercion ele (cons command curr))))
                  :multi  (let [[arr & xs] more
                                arr (if (vector? arr) arr [arr])]
                            (recur xs
                                   (reduce (fn [command entry]
                                             (cond (vector? entry)
                                                   (apply-with-coercion ele (cons command entry))

                                                   :else (apply-with-coercion ele [command entry])))
                                           command arr)))))
              (throw (Exception. (str "Option " slug " is not avaliable: " (-> options keys sort)))))))))
