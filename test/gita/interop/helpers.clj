(ns gita.interop.helpers
  (:require [clojure.java.io :as io])
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

(defn git-add-call [dir]
  (-> (Git/init)
      (.setDirectory dir)
      (.call)
      (.add)
      (.addFilepattern ".")
      (.call)))

(defn git-commit-call [dir]
  (-> (Git/init)
      (.setDirectory dir)
      (.call)
      (.commit)
      (.setMessage "hello world")
      (.call)))
