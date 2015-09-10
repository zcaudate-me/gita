(ns gita.api.repository
  (:require [clojure.java.io :as io]
            [gita.interop :as interop])
  (:import [org.eclipse.jgit.lib Repository Constants]
           [org.eclipse.jgit.treewalk TreeWalk CanonicalTreeParser AbstractTreeIterator]
           [org.eclipse.jgit.revwalk RevWalk RevCommit]
           org.eclipse.jgit.api.Git
           org.eclipse.jgit.storage.file.FileRepositoryBuilder
           org.eclipse.jgit.treewalk.filter.PathFilter
           java.util.Date
           java.io.File))

(def ^:dynamic *current-directory* nil)

(defn as-directory
  ^File [path]
  (if-let [^File curr-dir (io/as-file path)]
    (and (.isDirectory curr-dir)
         curr-dir)))

(defn root-dir [path]
  (if-let [curr-dir (as-directory path)]
    (if-let [git-dir (as-directory (str path "/.git"))]
      git-dir
      (recur (.getParent curr-dir)))))

(defn ^Repository repository
  ([] (repository (or *current-directory*
                      (System/getProperty "user.dir"))))
  ([path]
   (if-let [git-dir (root-dir path)]
     (let [repo (FileRepositoryBuilder/create git-dir)
           config (doto (.getConfig repo)
                    (.setString "remote", "origin", "fetch", "+refs/*:refs/*")
                    (.save))]
       repo)
     (throw (Exception. (str "The Git repository at '"
                             path "' could not be located."))))))

(defn repository? [obj]
  (instance? Repository obj))

(defn list-commits
  ([^Repository repo]
   (list-commits repo nil))
  ([^Repository repo ^String branch]
   (let [log (-> (Git. repo) (.log))
         log (if branch
               (.add log (.resolve repo branch))
               log)]
     (->> log
          (.call) (.iterator) (iterator-seq)
          (map (fn [^RevCommit commit] (hash-map :id (.getName commit)
                                                 :time (Date. (* 1000 (.getCommitTime commit))))))))))

(defn time->id
  ([repo t]
   (time->id repo nil t))
  ([repo branch ^Date t]
   (loop [[x & [y & _ :as more]] (reverse (list-commits repo branch))]
     (cond (nil? x)
           nil

           (nil? y)
           (if (.after t (:time x))
             (:id x))

           (and (or (.after  t (:time x))
                    (= t (:time x)))
                (.before t (:time y)))
           (:id x)

           :else (recur more)))))

(defn parse-head-string [x]
  (let [entry (subs x 4)] ;; 4 HEAD
    (if-let [carets (re-find #"^\^+$" entry)]
      (count carets)
      (if-let [number (re-find #"^[\^~](\d+)$" entry)]
        (Long/parseLong (second number))
        (throw (Exception. "Not yet supported"))))))

(defn resolve-id
  [^Repository repo branch x]
  (cond (instance? Date x)
        (recur repo branch (time->id repo branch x))

        (instance? Long x)
        (recur repo branch (time->id repo branch (Date. ^Long x)))

        (string? x)
        (cond (= x Constants/HEAD)
              (if-let [id (:id (first (list-commits repo branch)))]
                (.resolve repo id))

              (.startsWith ^String x Constants/HEAD)
              (if-let [id (->> (list-commits repo branch)
                               (drop (parse-head-string x))
                               first
                               :id)]
                (.resolve repo id))
              
              :else (.resolve repo x))))

(defn ^TreeWalk tree-walk
  ([repo] (tree-walk repo nil))
  ([^Repository repo opts]
   (let [{:keys [branch commit]} (merge {:branch Constants/MASTER :commit Constants/HEAD}
                                        opts)
         rwalk    (RevWalk. repo)
         cid      (resolve-id repo branch commit)]
     (if cid
       (let [rcommit  (.parseCommit rwalk cid)
             rtree    (.getTree rcommit)]
         (doto (TreeWalk. repo)
           (.addTree rtree)
           (.setRecursive true)))))))

(defn ^AbstractTreeIterator tree-parser
  ([repo] (tree-parser repo nil))
  ([^Repository repo opts]
   (let [{:keys [branch commit]} (merge {:branch Constants/MASTER :commit Constants/HEAD}
                                        opts)
         rwalk    (RevWalk. repo)
         cid      (resolve-id repo branch commit)]
     (if cid
       (let [reader   (.newObjectReader repo)
             rcommit  (.parseCommit rwalk cid)
             rtree    (.getTree rcommit)]
         (doto (CanonicalTreeParser.)
           (.reset reader (.getId rtree))))))))

(defn list-files
  ([repo] (list-files repo nil))
  ([^Repository repo opts]
   (if-let [walk (tree-walk repo opts)]
     (loop [walk walk
            out []]
       (if (.next walk)
         (recur walk (conj out (.getPathString walk)))
         out)))))

(defn raw
  [^Repository repo opts]
  (when-let [walk (tree-walk repo opts)]
    (.setFilter walk (PathFilter/create (:path opts)))
    (when (.next walk)
          (->> (.getObjectId walk 0)
               (.open repo)
               (.openStream)))))

(defn blob
  [^Repository repo id]
  (-> (.open repo
             id Constants/OBJ_BLOB)
      (.openStream)))
