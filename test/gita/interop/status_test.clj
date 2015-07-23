(ns gita.interop.status-test
  (:use midje.sweet)
  (:require [gita.interop.status]
            [gita.interop :as interop]
            [hara.object :as object]
            [clojure.java.io :as io]
            [gita.interop.helpers :refer :all]))


(fact "testing git status tracking"

 (let [path (str "/tmp/gita/" (java.util.UUID/randomUUID))
       tempdir (io/file path)]

   (with-out-str
     (print (git-status-call tempdir)))
   => "#status::{:clean? true, :uncommitted-changes? false}"

   (-> (git-status-call tempdir)
       (object/to-data))
   => {:clean? true, :uncommitted-changes? false}

   (spit (str path "/hello.txt") "hello")

   (-> (git-status-call tempdir)
       (object/to-data))
   => {:clean? false, :uncommitted-changes? false, :untracked #{"hello.txt"}}

   (spit (str path "/world.txt") "world")
   (-> (git-status-call tempdir)
       (object/to-data))
   => {:clean? false, :uncommitted-changes? false, :untracked #{"hello.txt" "world.txt"}}

   (delete-recursively tempdir)))


(fact "testing git status tracking after add call"

 (let [path (str "/tmp/gita/" (java.util.UUID/randomUUID))
       tempdir (io/file path)]
   (git-status-call tempdir)
   (spit (str path "/hello.txt") "hello")
   (git-add-call tempdir)
   (-> (git-status-call tempdir)
       (object/to-data))
   => {:clean? false, :uncommitted-changes? true,
       :uncommitted-changes #{"hello.txt"},
       :added #{"hello.txt"}}

   (delete-recursively tempdir)))


(fact "testing git status tracking after commit call"
 (let [path (str "/tmp/gita/" (java.util.UUID/randomUUID))
       tempdir (io/file path)]
   (git-status-call tempdir)
   (spit (str path "/hello.txt") "hello")
   (git-add-call tempdir)
   (git-commit-call tempdir)
   (-> (git-status-call tempdir)
       (object/to-data))
   => {:clean? true, :uncommitted-changes? false}

   (delete-recursively tempdir)))
