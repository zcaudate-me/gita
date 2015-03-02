(ns gita.interop.object-id-test
  (:require [gita.interop.object-id :refer :all]
            [midje.sweet :refer :all])
  (:import [org.eclipse.jgit.lib AnyObjectId ObjectId]))

(def id "794766b075c502112806f6731dc172dda37e0333")

(fact "returns the same id when it is transformed"
 (to-data (from-data id nil))
 => id)
