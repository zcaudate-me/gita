(ns gita.core
  (:require [gita.api.commands :as commands]
            [gita.api.repository :as repository]
            [gita.interop :as interop])
  (:import org.eclipse.jgit.api.Git))

(def ^:dynamic *dir* nil)

(defn git-help [all-commands]
  (println "\nSubtasks for git are:\n\n")
  (doseq [command (-> all-commands keys sort)]
    (println " " command))
  all-commands)

(defn git-command-help [cmd]
  (let [opts (reduce-kv (fn [m k res]
                          (assoc m k (commands/command-input res)))
                        {} (commands/command-options cmd))]
    (println "Options are: " opts)
    opts))

(defn run-command [pair dir]
  (if (vector? pair)
    (let [[ele inputs] pair
          cmd (if (-> ele :modifiers :static)
                (ele)
                (ele (Git. (repository/repository dir))))]
      (if (some #{:? :help} inputs)
        (git-command-help cmd)
        (-> cmd
            (commands/command-initialize-inputs inputs)
            (.call)
            (interop/to-data))))
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


(comment
  (git)
  (git :cd)
  (git :pwd)
  (git :init :?)
  (git :add :?)
  (git :rm :?)
  (git :stash :create)

  (git :add :filepattern ["."])
  (git :remove :help)
  (git :status)
  (git "/tmp/gita/init" :status)
  (git "." :status)
  (git :branch :create))
