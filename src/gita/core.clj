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


(defn run-command [pair dir]
  (if (vector? pair)
    (let [[ele inputs] pair
          cmd (if (-> ele :modifiers :static)
                (ele Git)
                (ele (Git. (repository/repository dir))))
          cmd (commands/command-initialize-inputs cmd inputs)]
      (-> (.call cmd)
          (interop/to-data)))
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
  (git "." :status)
  (git :branch :create))
