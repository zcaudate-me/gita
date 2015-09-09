(ns gita.api.repository-test
  (:use midje.sweet)
  (:require [clojure.java.io :as io]
            [gita.api.repository :refer :all])
  (:import org.eclipse.jgit.api.Git))


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
