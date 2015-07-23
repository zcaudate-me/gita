(ns gita.api.repository
  (:require [clojure.java.io :as io])
  (:import org.eclipse.jgit.storage.file.FileRepositoryBuilder
           org.eclipse.jgit.lib.Constants
           org.eclipse.jgit.treewalk.filter.PathFilter
           org.eclipse.jgit.treewalk.TreeWalk
           [org.eclipse.jgit.revwalk RevWalk RevCommit]
           org.eclipse.jgit.api.Git))

(def ^:dynamic *current-directory* nil)

(defn as-directory
  ^java.io.File [path]
  (if-let [^java.io.File curr-dir (io/as-file path)]
    (and (.isDirectory curr-dir)
         curr-dir)))

(defn root-dir [path]
  (if-let [curr-dir (as-directory path)]
    (if-let [git-dir (as-directory (str path "/.git"))]
      git-dir
      (recur (.getParent curr-dir)))))

(defn repository
  ([] (repository (or *current-directory* (System/getProperty "user.dir"))))
  ([path]
   (if-let [git-dir (root-dir path)]
     (let [repo (FileRepositoryBuilder/create git-dir)
           config (doto (.getConfig repo)
                    (.setString "remote", "origin", "fetch", "+refs/*:refs/*")
                    (.save))]
       repo)
     (throw (Exception. (str "The Git repository at '"
                             path "' could not be located."))))))

(defn list-commits
  ([] (list-commits (repository)))
  ([repo]
   (->> (Git. repo) (.log) (.call) (.iterator) (iterator-seq)
        (map #(hash-map :sha (.getName %)
                        :time (java.util.Date. (* 1000 (.getCommitTime %))))))))

(defn time->id [repo ^java.util.Date t]
  (loop [[x & [y & _ :as more]] (reverse (list-commits repo))]
    (cond (nil? x)
          nil

          (nil? y)
          (if (.after t (:time x))
            (:sha x))

          (and (or (.after  t (:time x))
                   (= t (:time x)))
               (.before t (:time y)))
          (:sha x)

          :else (recur more))))

(defn resolve-id [repo x]
  (cond (instance? java.util.Date x) (recur repo (time->id repo x))
        (instance? java.lang.Long x) (recur repo (time->id repo (java.util.Date. x)))
        (string? x) (.resolve repo x)))

(defn list-files
  ([] (list-files (repository)))
  ([repo]
   (list-files repo Constants/HEAD))
  ([repo version]
   (let [rwalk    (RevWalk. repo)
         cid      (resolve-id repo version)]
     (if cid
       (let [rcommit  (.parseCommit rwalk cid)
             rtree    (.getTree rcommit)
             treewalk (doto (TreeWalk. repo)
                        (.addTree rtree)
                        (.setRecursive true))]
         (loop [iter treewalk
                out []]
           (if (.next iter)
             (recur iter (conj out (.getPathString iter)))
             out)))))))

(defn stream
  ([repo path]
   (stream repo path Constants/HEAD))
  ([repo path version]
   (let [rwalk    (RevWalk. repo)
         cid      (resolve-id repo version)]
     (if cid
       (let [rcommit  (.parseCommit rwalk cid)
             rtree    (.getTree rcommit)
             treewalk (doto (TreeWalk. repo)
                        (.addTree rtree)
                        (.setRecursive true)
                        (.setFilter (PathFilter/create path)))]
         (when (.next treewalk)
           (->> (.getObjectId treewalk 0)
                (.open repo)
                (.openStream))))))))

(comment
  (require '[rewrite-clj.zip :as source])
  (source/of-string (slurp (stream (repository) "project.clj")))

  (list-files (repository) #inst "2015-06-13T19:51:14.000-00:00")
  (list-files (repository) #inst "2015-06-13T19:51:19.000-00:00")
  (list-files (repository) #inst "2015-06-13T19:51:41.000-00:00")

  ({:time #inst "2015-06-13T19:51:31.000-00:00", :commit "48911d39d335afbf89ce314d412f47d2884750e8"} {:time #inst "2015-06-13T19:51:14.000-00:00", :commit "3ce4dff444260ccdc93d08d550fd1c3a4d44b1ea"})
  (type (first a))
  (str a)
  (.getAuthorIdent (first a))
  (list-files (repository))
  (slurp )
)
