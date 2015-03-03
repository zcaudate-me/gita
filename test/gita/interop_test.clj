(ns gita.interop-test
  (:require [gita.interop :refer :all]
            [midje.sweet :refer :all]
            [gita.api.repository :as repository])
  (:import org.eclipse.jgit.api.Git))

(comment

  (.? (Git. (repository/repository)) :name)
  )
