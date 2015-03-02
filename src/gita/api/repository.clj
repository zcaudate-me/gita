(ns gita.api.repository
  (:require [clojure.java.io :as io])
  (:import org.eclipse.jgit.storage.file.FileRepositoryBuilder))

(def ^:dynamic *current-directory* nil)

(defn as-directory [path]
  (if-let [curr-dir (io/as-file path)]
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
     (FileRepositoryBuilder/create git-dir)
     (throw (Exception. (str "The Git repository at '"
                             path "' could not be located."))))))
