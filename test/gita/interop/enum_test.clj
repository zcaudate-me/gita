(ns gita.interop.enum-test
  (:require [gita.interop.enum :as enum]
            [gita.interop :as interop]
            [midje.sweet :refer :all]))

(fact "enum-values"
  (->> (enum/enum-values org.eclipse.jgit.api.ResetCommand$ResetType)
       (map str))
  => (just ["SOFT" "MIXED" "HARD" "MERGE" "KEEP"] :in-any-order))

(fact "interop/to-data"
 (interop/to-data org.eclipse.jgit.patch.BinaryHunk$Type/DELTA_DEFLATED)
 => "DELTA_DEFLATED")

(fact "interop/from-data"
 (interop/from-data  "DELTA_DEFLATED" org.eclipse.jgit.patch.BinaryHunk$Type)
 => org.eclipse.jgit.patch.BinaryHunk$Type/DELTA_DEFLATED

 (interop/from-data "HARD" org.eclipse.jgit.api.ResetCommand$ResetType)
 => org.eclipse.jgit.api.ResetCommand$ResetType/HARD

 (interop/from-data "NONE_EXISTENT" org.eclipse.jgit.api.ResetCommand$ResetType)
 => (throws))
