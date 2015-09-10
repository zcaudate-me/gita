(ns gita.api.repository-test
  (:use midje.sweet)
  (:require [clojure.java.io :as io]
            [gita.api.repository :refer :all])
  (:import org.eclipse.jgit.api.Git))


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
                                     {:commit "HEAD^3"})))

  (type (.getChangeType entry))
  
  (first (filter #(-> % (.getChangeType) (not= org.eclipse.jgit.diff.DiffEntry$ChangeType/MODIFY))
                 (list-difference (repository)
                                  {}
                                  {:commit "HEAD^3"})))
  
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

(comment
  (list-files (repository) "master" #inst "2015-04-08T06:16:39.000-00:00")

  (slurp (stream (repository) ".gitignore"))
  
  (filter #(-> % :id (= "1cf1c3b441ca3fcc8eb363f663d1da3e54ff0a01"))
          (list-commits (repository) "gh-p"))
  
  (list-commits (repository) "master")
  (list-commits (repository) "gh-pages")
  ({:time #inst "2015-09-08T06:16:39.000-00:00", :sha "1cf1c3b441ca3fcc8eb363f663d1da3e54ff0a01"}
   {:time #inst "2015-09-08T06:16:20.000-00:00", :sha "493fab54e0abed074afa9017d00555b75fe5052b"})


  (time->id (repository) "gh-pages" #inst "2015-09-08T06:16:30.000-00:00")
  "493fab54e0abed074afa9017d00555b75fe5052b"
  (time->id (repository) "master" #inst "2015-09-08T06:16:30.000-00:00")
  "e6a2401f173976c465fe49fd3480d8dd7954ba05"

  (.* "oeuoeuoe" :name)
  
  
  
  (->> (repository)
       (Git.)
       (.log)
       (.call)
       (.iterator)
       (iterator-seq)))


(comment
  (defn git-init [dir]
    (-> (Git/init)
        (.setDirectory dir)
        (.call)))
  
  
  (repository "/tmp/git/repo-test"))
