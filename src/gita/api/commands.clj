(ns gita.api.commands
  (:require [hara.reflect :as reflect]
            [hara.common.string :refer [to-string]]
            [hara.string.case :as case]
            [clojure.string :as string])
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

(defn apply-with-coercion
  ([{:keys [params] :as ele} args]
   (println params)
   (apply ele args)))

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
