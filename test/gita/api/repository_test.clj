(ns gita.api.repository-test
  (:use midje.sweet)
  (:require [clojure.java.io :as io]
            [gita.api.repository :refer :all])
  (:import org.eclipse.jgit.api.Git))


(defn git-init [dir]
  (-> (Git/init)
      (.setDirectory dir)
      (.call)))

(repository "/tmp/git/repo-test")
