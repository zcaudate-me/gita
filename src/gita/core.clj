(ns gita.core
  (:require [gita.api.commands :as commands]
            [gita.api.repository :as repository]
            [gita.interop :as interop]
            [hara.object :as object]
            [hara.namespace.import :as ns])
  (:import org.eclipse.jgit.api.Git))

(ns/import gita.api.repository [repository])

(defonce ^:dynamic *dir* nil)

(defn git-help [all-commands]
  (let [out (-> all-commands keys sort vec)]
    (println "\nSubtasks for git are:\n\n")
    (println out)
    out))

(defn git-command-help [cmd]
  (let [opts (reduce-kv (fn [m k res]
                          (assoc m k (commands/command-input res)))
                        {} (commands/command-options cmd))]
    (println "Options are: " opts)
    opts))

(defn wrap-help [f]
  (fn  [cmd inputs]
    (if (some #{:? :help} inputs)
      (git-command-help cmd)
      (f cmd inputs))))

(defn wrap-result [f]
  (fn [cmd inputs]
    (let [res (->> (filter #(not= :& %) inputs)
                   (f cmd))]
      (if (some #{:&} inputs)
        res
        (object/to-data res)))))

(defn run-base [cmd inputs]
  (-> ^java.util.concurrent.Callable (commands/command-initialize-inputs cmd inputs)
      (.call)))

(defn run-command [pair dir]
  (if (vector? pair)
    (let [[ele inputs] pair
          cmd (if (-> ele :modifiers :static)
                (ele)
                (ele (Git. (repository/repository dir))))]

      ((-> run-base
           wrap-result
           wrap-help) cmd inputs))
    pair))

(defn git
  ([] (git :help))
  ([dir? & args]
   (let [all-commands (commands/git-all-commands)
         curr (System/getProperty "user.dir")
         [dir [c & cs :as args]] (if (keyword? dir?)
                                    [(or *dir* curr)
                                     (cons dir? args)]
                                    [(do (alter-var-root #'*dir* (fn [x] dir?))
                                         dir?)
                                     args])]
     (cond (= :help c)
           (git-help all-commands)

           (= :cd c)
           (alter-var-root #'*dir* (fn [x] (first cs)))

           (= :pwd c)
           (or *dir* curr)

           :else
           (-> (commands/command all-commands args)
               (run-command dir))))))
