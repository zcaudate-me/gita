(ns gita.interop.status-test
  (:use midje.sweet)
  (:require [gita.interop :as interop]
            [hara.object :as object]
            [clojure.java.io :as io]
            [gita.interop.helpers :refer :all]))

(defn display
  [m]
  (reduce-kv (fn [out k v]
               (if (and (or (instance? java.util.Collection v)
                            (instance? java.util.Map v))
                        (empty? v))
                 out
                 (assoc out k v)))
             {}
             m))

(fact "testing git status tracking"
  (def path (str "/tmp/gita/" (java.util.UUID/randomUUID)))
  (def tempdir (io/file path))

  (with-out-str
    (print (git-status-call tempdir)))
  => string?

  (-> (git-status-call tempdir)
      (object/to-data)
      (display))
  => {:clean? true}

  (spit (str path "/hello.txt") "hello")

  (-> (git-status-call tempdir)
      (object/to-data)
      (display))
  =>  {:untracked ["hello.txt"]}

  (spit (str path "/world.txt") "world")
  (-> (git-status-call tempdir)
      (object/to-data)
      (display))
  => (contains {:untracked (contains ["hello.txt" "world.txt"] :in-any-order)})

  (delete-recursively tempdir))


(fact "testing git status tracking after add call"
  (def path (str "/tmp/gita/" (java.util.UUID/randomUUID)))
  (def tempdir (io/file path))

  (git-status-call tempdir)
  (spit (str path "/hello.txt") "hello")
  (git-add-call tempdir)
  (-> (git-status-call tempdir)
      (object/to-data)
      (display))
  => {:added ["hello.txt"], :uncommitted-changes ["hello.txt"]}

  (delete-recursively tempdir))


(fact "testing git status tracking after commit call"
  (def path (str "/tmp/gita/" (java.util.UUID/randomUUID)))
  (def tempdir (io/file path))

  (git-status-call tempdir)
  (spit (str path "/hello.txt") "hello")
  (git-add-call tempdir)
  (git-commit-call tempdir)
  (-> (git-status-call tempdir)
      (object/to-data)
      display)
  => {:clean? true}

  (delete-recursively tempdir))
