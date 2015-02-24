(ns gita.core
  (:require [clojure.java.io :as io]
            [hara.reflect :as reflect]
            [hara.common.string :refer [to-string]]
            [hara.string.case :as case]
            [clojure.string :as string])
  (:import org.eclipse.jgit.api.Git
           org.eclipse.jgit.lib.RepositoryBuilder
           org.eclipse.jgit.storage.file.FileRepositoryBuilder))

(def ^:dynamic *current-directory* nil)

(defn name-from-uri
  "Given a URI to a Git resource, derive the name (for use in cloning to a directory)"
  [uri]
  (second (re-find #"/([^/]*)\.git$" uri)))

(defn as-directory [path]
  (if-let [curr-dir (io/as-file path)]
    (and (.isDirectory curr-dir)
         curr-dir)))

(defn git-root-dir [path]
  (if-let [curr-dir (as-directory path)]
    (if-let [git-dir (as-directory (str path "/.git"))]
      git-dir
      (recur (.getParent curr-dir)))))

(defn git-repo
  ([] (git-repo (or *current-directory* (System/getProperty "user.dir"))))
  ([path]
    (if-let [git-dir (git-root-dir path)]
      (FileRepositoryBuilder/create git-dir)
      (throw
       (Exception. (str "The Git repository at '" path "' could not be located."))))))

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

(defn git-help [commands]
  (println "\nSubtasks for git are:\n\n")
  (doseq [command (-> commands keys sort)]
    (println " " command))
  commands)

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

(defn command-initialize-inputs
  [command inputs]
  (let [options (command-options command)]
    (loop [[slug & more] inputs
           command command]
      (cond (nil? slug) command

            :else
            (if-let [field (get options slug)]
              (let [ele (:element field)
                    ptypes (:params ele)]
                (case (:type field)
                  :single (let [curr (take (count ptypes) more)
                                nxt  (drop (count ptypes) more)]
                            (recur nxt (apply ele command curr)))
                  :multi  (let [[arr & xs] more
                                arr (if (vector? arr) arr [arr])]
                            (recur xs
                                   (reduce (fn [command entry]
                                             (cond (vector? entry)
                                                   (apply ele command entry)

                                                   :else (ele command entry)))
                                           command arr)))))
              (throw (Exception. (str "Option " slug " is not avaliable: " (-> options keys sort)))))))))

(defn git-command-help [commands inputs])

(defn inputs->command [inputs]
  (->> inputs
       (map to-string)
       (string/join "-")
       (case/camel-case)
       (vector :#)
       (reflect/query-class Git)))

(defn git-command [commands [input & more]]
  (if-let [subcommands (get commands input)]
    (cond (empty? subcommands)
          (inputs->command (cons input subcommands))

          (> (count subcommands) 1)
          (if (get subcommands (first more))
            (inputs->command (cons input [(first more)]))
            (do (println (str "Options for " input " are: " subcommands))
                subcommands))

          (= (count subcommands) 1)
          (inputs->command (cons input subcommands)))))

(defn wrap-run-command [f]
  (if (instance? hara.reflect.types.element.Element ele)
    (if (-> ele :modifiers :static)
      (.call (ele Git))
      (.call (ele (Git. (git-repo)))))))

(defn git [& [input & more :as inputs]]
  (let [commands (git-all-commands)]
    (cond (or (nil? input)
              (= :help input))
          (git-help commands)

          (= :cd input)
          (alter-var-root #'*current-directory*
                          (fn [x] (first more)))

          (= :pwd input)
          (or *current-directory* (System/getProperty "user.dir"))

          (some #(= :help %) inputs)
          (git-command-help commands inputs)

          :else
          (wrap-run-command (git-command commands inputs)))))



(comment
  (git :cd)
  (git :pwd)
  (git :status)
  (git :branch :create)
  (-> (command-options org.eclipse.jgit.api.AddCommand)
      first
      keys)
  (-> (list-git-commands) keys sort)

  (count "org.eclipse.jgit.api.")



  (.call (.status (Git. (git-repo (System/getProperty "user.dir")))))

  (reflect/query-class (.call (.status (Git. (git-repo (System/getProperty "user.dir"))))) [])

  (.getUncommittedChanges (.call (.status (Git. (git-repo (System/getProperty "user.dir"))))))

  (.call (doto (Git/init)
           (.setDirectory (io/file (str (System/getProperty "user.dir") "/a/b/c/d")))))


  (defmulti git (fn [& [command & args]] command))

  (defmethod git :default [& args]
    (println "HELP:"))

  (git :init :help)

  (def status (.call ((git :status) (Git. (git-repo)))))

  (defn java->clojure [name]
    (let [nname (cond (re-find #"^get.+" name)
                      (subs name 3)

                      (re-find #"^is.+" name)
                      (str (subs name 2) "?")

                      (re-find #"^has.+" name)
                      (str (subs name 3) "?")

                      :else name)]
      (case/spear-case nname)))

  (defn status->map [^org.eclipse.jgit.api.Status status]
    (let [methods (reflect/query-class status [:method])]
      (->> methods
           (map (fn [ele] [(-> ele :name java->clojure keyword)
                          (ele status)]))
           (reduce (fn [m [k v]]
                     (if (and (or (instance? java.util.Collection v)
                                  (instance? java.util.Map v))
                              (.isEmpty v))
                       m
                       (assoc m k v)))
                   {}))))

  (defprotocol IData
    (to-data [obj]))

  (extend-protocol IData
    Object
    (to-data [obj] obj))

  (extend-protocol IData
    org.eclipse.jgit.api.Status
    (to-data [obj] (status->map obj)))

  (def m (status->map status))
  (type (:conflicting-stage-state m))
  (.isEmpty (:conflicting-stage-state m))
  (.%> (:conflicting-stage-state m))
  (coll? #{})
  (empty? #{})

  (:all (git :init))


  (git :branch)
)
