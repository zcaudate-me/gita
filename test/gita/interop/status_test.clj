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
