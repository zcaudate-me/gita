(ns gita.interop.status-test
  (:use midje.sweet)
  (:require [gita.interop.status]
            [gita.interop :as interop]
            [clojure.java.io :as io])
  (:import org.eclipse.jgit.api.Git))

(defn delete-recursively [^java.io.File f]
  (when (.isDirectory f)
    (doseq [f2 (.listFiles f)]
      (delete-recursively f2)))
  (io/delete-file f))

(defn git-status-call [dir]
  (-> (Git/init)
      (.setDirectory dir)
      (.call)
      (.status)
      (.call)))

(fact "testing git status tracking"

 (let [path (str "/tmp/gita/" (java.util.UUID/randomUUID))
       tempdir (io/file path)]

   (with-out-str
     (print (git-status-call tempdir)))
   => "#status::{:clean? true, :uncommitted-changes? false}"

   (-> (git-status-call tempdir)
       (interop/to-data))
   => {:clean? true, :uncommitted-changes? false}

   (spit (str path "/hello.txt") "hello")

   (-> (git-status-call tempdir)
       (interop/to-data))
   => {:clean? false, :uncommitted-changes? false, :untracked #{"hello.txt"}}

   (spit (str path "/world.txt") "world")
   (-> (git-status-call tempdir)
       (interop/to-data))
   => {:clean? false, :uncommitted-changes? false, :untracked #{"hello.txt" "world.txt"}}

   (delete-recursively tempdir)))

(defn git-add-call [dir]
  (-> (Git/init)
      (.setDirectory dir)
      (.call)
      (.add)
      (.addFilepattern ".")
      (.call)))

(fact "testing git status tracking after add call"

 (let [path (str "/tmp/gita/" (java.util.UUID/randomUUID))
       tempdir (io/file path)]
   (git-status-call tempdir)
   (spit (str path "/hello.txt") "hello")
   (git-add-call tempdir)
   (-> (git-status-call tempdir)
       (interop/to-data))
   => {:clean? false, :uncommitted-changes? true,
       :uncommitted-changes #{"hello.txt"},
       :added #{"hello.txt"}}

   (delete-recursively tempdir)))

(defn git-commit-call [dir]
  (-> (Git/init)
      (.setDirectory dir)
      (.call)
      (.commit)
      (.setMessage "hello world")
      (.call)))

 (let [path (str "/tmp/gita/" (java.util.UUID/randomUUID))
       tempdir (io/file path)]
   (git-status-call tempdir)
   (spit (str path "/hello.txt") "hello")
   (git-add-call tempdir)
   (git-commit-call tempdir))
