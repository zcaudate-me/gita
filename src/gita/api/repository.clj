(ns gita.api.repository
  (:require [clojure.java.io :as io]
            [gita.interop :as interop])
  (:import org.eclipse.jgit.storage.file.FileRepositoryBuilder
           org.eclipse.jgit.lib.Constants
           org.eclipse.jgit.treewalk.filter.PathFilter
           [org.eclipse.jgit.treewalk TreeWalk CanonicalTreeParser]
           [org.eclipse.jgit.revwalk RevWalk RevCommit]
           org.eclipse.jgit.api.Git
           org.eclipse.jgit.lib.Repository
           [org.eclipse.jgit.diff HistogramDiff RawTextComparator RawText]
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

(defn repository
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

(defn tree-parser
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

(defrecord Difference [])

(defn difference
  [^Repository repo old-id new-id]
  (let [get-text (fn [id] (if id
                            (RawText. (.getCachedBytes (.open repo id Constants/OBJ_BLOB)))
                            RawText/EMPTY_TEXT))
        old-text (get-text old-id)
        new-text (get-text new-id)]
    (-> (HistogramDiff.)
        (.diff RawTextComparator/DEFAULT old-text new-text))))

(defn list-difference
  ([repo old new]
   (-> (Git. repo)
       (.diff)
       (.setOldTree (tree-parser repo old))
       (.setNewTree (tree-parser repo new))
       (.call))))


(defn blob [repo id]
  (-> (.open (repository)
             id Constants/OBJ_BLOB)
      (.openStream)))

(comment
  (diff-histogram
            (repository)
            (.resolve (repository) "b07d752684b9be0bbd710d770c4d3a5ebc53f09d")
            (.resolve (repository) "92f123615916b5cc3b9808c1ea784f11cf39508a")
            )

  (subvec  
   (vec (line-seq (clojure.java.io/reader (blob (repository) (.resolve (repository) "b07d752684b9be0bbd710d770c4d3a5ebc53f09d")))))
   13
   14)
  ["#*"]
  
  (subvec  
   (vec (line-seq (clojure.java.io/reader (blob (repository) (.resolve (repository) "92f123615916b5cc3b9808c1ea784f11cf39508a")))))
   13
   15)
  ["#*" "*#"]

  (def e (first (diff-histogram
                 (repository)
                 (.resolve (repository) "b07d752684b9be0bbd710d770c4d3a5ebc53f09d")
                 (.resolve (repository) "92f123615916b5cc3b9808c1ea784f11cf39508a"))))

  (type (diff-histogram
         (repository)
         (.resolve (repository) "b07d752684b9be0bbd710d770c4d3a5ebc53f09d")
         (.resolve (repository) "92f123615916b5cc3b9808c1ea784f11cf39508a")))
  
  (.getBeginA e)
  (.getEndB e)


  
  (def out (java.io.ByteArrayOutputStream.))
  
  (defn df [repo]
    (doto (DiffFormatter. out)
      (.setRepository repo)))

  
  (def entry (first (list-difference (repository)
                                     {}
                                     {:commit "HEAD^1"})))

  
  (-> (.open (repository)
             (.resolve (repository) "b07d752684b9be0bbd710d770c4d3a5ebc53f09d")
             Constants/OBJ_BLOB)
      (.getCachedBytes)
      ;;(slurp)
      )
  "/target\n/classes\n/checkouts\npom.xml\npom.xml.asc\n*.jar\n*.class\n/.lein-*\n/.nrepl-port\n.hgignore\n.hg/\n.#*\n.DS_Store\n#*"

  (-> (.open (repository)
             (.resolve (repository) "92f123615916b5cc3b9808c1ea784f11cf39508a"))
      (.openStream)
      (slurp))
  "/target\n/classes\n/checkouts\npom.xml\npom.xml.asc\n*.jar\n*.class\n/.lein-*\n/.nrepl-port\n.hgignore\n.hg/\n.#*\n.DS_Store\n#*\n*#\n"
  
  (.* (.open (repository)
             (.resolve (repository) "b07d752684b9be0bbd710d770c4d3a5ebc53f09d"))
      :name)
  ("clone" "copyTo" "data" "equals" "finalize" "getBytes" "getCachedBytes" "getClass" "getSize" "getType" "hashCode" "isLarge" "notify" "notifyAll" "openStream" "toString" "type" "wait")
  
  
  
  (.* entry :name)
  entry
  
  
  {:change-type "MODIFY", :score 0, :old-path ".gitignore", :new-mode "100644", :tree-filter-marks 0, :new-path ".gitignore", :old-id "92f123615916b5cc3b9808c1ea784f11cf39508a", :old-mode "100644", :new-id "b07d752684b9be0bbd710d770c4d3a5ebc53f09d"}
  
  ("changeType" "clone" "equals" "finalize" "getChangeType" "getClass" "getId" "getMode" "getNewId" "getNewMode" "getNewPath" "getOldId" "getOldMode" "getOldPath" "getPath" "getScore" "getTreeFilterMarks" "hashCode" "isMarked" "newId" "newMode" "newPath" "notify" "notifyAll" "oldId" "oldMode" "oldPath" "score" "toString" "treeFilterMarks" "wait")

  (.format (df (repository))
           entry)

  (println (.toString out))
  
  (list-files (repository) {:commit #inst "2015-06-13T19:51:14.000-00:00"})
  (list-files (repository) {:commit #inst "2015-06-13T19:51:19.000-00:00"})

  (list-files (repository) {:commit "HEAD~1"
                            :branch "gh-pages"})


  (println (take 3 (list-difference (repository)
                                    {}
                                    {:commit "HEAD^1"})))
  
  
  
  (resolve-id (repository) "master" "HEAD")
  ;;#id "e6a2401f173976c465fe49fd3480d8dd7954ba05"
  (resolve-id (repository) "gh-pages" "HEAD")
  ;;#id "1cf1c3b441ca3fcc8eb363f663d1da3e54ff0a01"
  
  (.resolve (repository) "HEAD")

  ;;(git :log)

  ;;(git :checkout)
  (.resolve (repository) "HEAD")

  (.getBranch (repository))
  => "master"
  
  (list-files (repository) {:commit "HEAD"
                            :branch "gh-pages"})

  (slurp (raw (repository) {:commit "1cf1c3b441ca3fcc8eb363f663d1da3e54ff0a01"
                            :branch "gh-pages"
                            :path ".travis.yml"})))
