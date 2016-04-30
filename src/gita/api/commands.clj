(ns gita.api.commands
  (:require [hara.reflect :as reflect]
            [hara.reflect.util :as util]
            [hara.common.string :refer [to-string]]
            [hara.class.inheritance :as inheritance]
            [hara.string.case :as case]
            [clojure.string :as string]
            [hara.object :as object]
            [gita.interop :as interop])
  (:import org.eclipse.jgit.api.Git))

(defn may-coerce [^Class param arg]
  (let [^Class targ (type arg)
        {:keys [types from-map from-string from-vector]} (object/meta-write param)]
    (cond (util/param-arg-match param targ) arg

          from-map (from-map arg param)
          from-string (from-string arg param)
          from-vector (from-vector arg param)

          :else
          (throw (Exception. (str "Cannot convert value " arg
                                  " of type " (.getName targ) " to " (.getName param)) )))))

(defn apply-with-coercion
  ([{:keys [params] :as ele} args]
   (apply ele (map may-coerce params args))))

(defn git-all-commands []
  (->> (reflect/query-class Git [:name :type])
       (map (fn [m] (assoc m :command (to-string (:type m)))))
       (filter (fn [m] (.endsWith ^String (:command m) "Command")))
       (map (fn [m] (-> m :name (case/spear-case) (string/split #"-") (->> (map keyword)))))
       (reduce (fn [m [root sub]]
                 (if sub
                   (update-in m [root] (fnil conj #{}) sub)
                   (assoc-in m [root] #{})))
               {})))

(defn command-options [command]
  (->> (reflect/query-class command [:public :method (type command)])
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
       (reduce (fn [m [k val]]
                 (if (or (not (get m k))
                         (-> val :params second object/meta-write (dissoc :class) empty? not))
                   (assoc m k val)
                   m))
               {})))

(defn command-input [opt]
  (let [param    (-> opt :element :params second)
        {:keys [to-map to-string to-vector]} (object/meta-read param)
        out  (cond (-> param
                       (inheritance/ancestor-list)
                       set
                       (get Enum))
                   (->> param object/enum-values (map object/to-data) set)

                   to-map java.util.Map
                   to-string String
                   to-vector java.util.List
                   
                   :else param)]
    (case (:type opt)
      :single out
      :multi [out])))

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


(defn git-element [keywords]
  (->> keywords
       (map to-string)
       (string/join "-")
       (case/camel-case)
       (vector :#)
       (reflect/query-class Git)))

(defn command [all-commands [input & more]]
  (if-let [subcommands (get all-commands input)]
    (cond (empty? subcommands)
          [(git-element (cons input subcommands)) more]

          (> (count subcommands) 1)
          (if (get subcommands (first more))
            [(git-element (cons input [(first more)])) (rest more)]
            (do (println (str "Options for " input " are: " subcommands))
                subcommands))

          (= (count subcommands) 1)
          [(git-element (cons input subcommands)) more])
    (throw (Exception. (str "Cannot find " input " in the list of Git commands")))))
