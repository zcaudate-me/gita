(ns gita.api.commands
  (:require [hara.reflect :as reflect]
            [hara.reflect.util :as util]
            [hara.common.string :refer [to-string]]
            [hara.string.case :as case]
            [clojure.string :as string]
            [hara.object :as object]
            [gita.interop :as interop])
  (:import org.eclipse.jgit.api.Git))

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
       (reduce (fn [m [k val]]
                 (if (or (not (get m k))
                         (-> val :params second object/meta-object :from-data))
                   (assoc m k val)
                   m))
               {})))

(defn command-input [opt]
  (let [param    (-> opt :element :params second)
        mobj (object/meta-object param)
        out  (cond (= Enum (:class mobj))
                   (->> param object/enum-values (map object/to-data) set)

                   (:from-data mobj)
                   (first (:types mobj))

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
                            (recur nxt (object/apply-with-coercion ele (cons command curr))))
                  :multi  (let [[arr & xs] more
                                arr (if (vector? arr) arr [arr])]
                            (recur xs
                                   (reduce (fn [command entry]
                                             (cond (vector? entry)
                                                   (object/apply-with-coercion ele (cons command entry))

                                                   :else (object/apply-with-coercion ele [command entry])))
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
