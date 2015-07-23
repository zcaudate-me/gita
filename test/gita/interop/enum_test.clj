(ns gita.interop.enum-test
  (:require [hara.object :as object]
            [midje.sweet :refer :all]))

(fact "enum-values"
  (->> (object/enum-values org.eclipse.jgit.api.ResetCommand$ResetType)
       (map str))
  => (just ["SOFT" "MIXED" "HARD" "MERGE" "KEEP"] :in-any-order))

(fact "interop/to-data"
  (object/to-data org.eclipse.jgit.patch.BinaryHunk$Type/DELTA_DEFLATED)
  => "DELTA_DEFLATED")

(fact "interop/from-data"
  (object/from-data  "DELTA_DEFLATED" org.eclipse.jgit.patch.BinaryHunk$Type)
  => org.eclipse.jgit.patch.BinaryHunk$Type/DELTA_DEFLATED

  (object/from-data "HARD" org.eclipse.jgit.api.ResetCommand$ResetType)
  => org.eclipse.jgit.api.ResetCommand$ResetType/HARD

  (object/from-data "NONE_EXISTENT" org.eclipse.jgit.api.ResetCommand$ResetType)
  => (throws))
