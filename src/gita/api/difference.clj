(ns gita.api.difference
  (:require [gita.api.repository :as repository]
            [hara.object :as object])
  (:import [org.eclipse.jgit.diff HistogramDiff RawTextComparator RawText
            DiffEntry DiffEntry$ChangeType Edit EditList]
           [org.eclipse.jgit.lib Repository Constants AbbreviatedObjectId]
           org.eclipse.jgit.api.Git))

(defonce null-id "0000000000000000000000000000000000000000")

(defn enum->keyword [enum]
  (keyword (.toLowerCase (str enum))))

(defrecord Change [])

(defn format-change
  [^Repository repo ^Edit edit old-id new-id]
  (let [type (enum->keyword (.getType edit))]
    (case type
      :insert nil
      :delete nil
      :replace nil
      :empty nil)))

(defrecord Difference [])

(defn difference
  [^Repository repo entry old-id new-id]
  (let [get-text (fn [^AbbreviatedObjectId id]
                   (if id
                     (-> repo
                         (.open (.toObjectId id) Constants/OBJ_BLOB)
                         (.getCachedBytes)
                         (RawText.))
                     RawText/EMPTY_TEXT))
        old-text (get-text old-id)
        new-text (get-text new-id)
        changes  (-> (HistogramDiff.)
                     (.diff RawTextComparator/DEFAULT old-text new-text))]
    changes))

(defn format-entry
  [^Repository repo ^DiffEntry entry]
  (let [type (enum->keyword (.getChangeType entry))]
    (case type
      :modify (difference repo (.getOldId entry) (.getNewId entry))

      :add   (difference repo nil (.getNewId entry))

      :delete (difference repo (.getOldId entry) nil)
      
      :rename (throw (Exception. "RENAME NOT SUPPORTED"))

      :copy   (throw (Exception. "COPY NOT SUPPORTED")))))

(defn list-difference
  ([^Repository repo old new]
   (-> (Git. repo)
       (.diff)
       (.setOldTree (repository/tree-parser repo old))
       (.setNewTree (repository/tree-parser repo new))
       (.call))))

(defn format-diff
  [repo entries]
  (map (partial format-entry repo) entries))



(comment
  (.* (ffirst (list-difference
               (repository/repository)
               {:commit "HEAD~3"}
               {}
               ))
      :name)
  ("after" "before" "beginA" "beginB" "clone" "endA" "endB" "equals" "extendA" "extendB" "finalize" "getBeginA" "getBeginB" "getClass" "getEndA" "getEndB" "getLengthA" "getLengthB" "getType" "hashCode" "isEmpty" "notify" "notifyAll" "swap" "toString" "wait")

  {:type     :modify
   :old-path "src/gita/api/seek.clj"
   :old-id   "173ce2e785437f37273a3ba58be2f30819ae4b48"
   :changes  [{:type :replace
               :old {:lines []
                     :text  []}
               :new {:lines []
                     :text  []}}
              {:type   :delete
               :old {:lines []
                     :text  []}
               :new {:lines []
                     :text  []}}
              {:type :insert
               :old {:lines []
                     :text  []}
               :new {:lines []
                     :text  []}}]}
  
  (format-diff
   (repository/repository)
   (list-difference
    (repository/repository)
    {:commit "HEAD~3"}
    {}
    ))
  
  
  (;;[#<Edit REPLACE(13-15,13-14)>] [#<Edit REPLACE(0-1,0-1)> #<Edit REPLACE(6-9,6-8)>] [#<Edit DELETE(7-8,7-7)> #<Edit REPLACE(71-72,70-71)> #<Edit REPLACE(77-78,76-77)> #<Edit REPLACE(79-80,78-79)>] [#<Edit REPLACE(1-3,1-2)> #<Edit REPLACE(6-7,5-6)> #<Edit REPLACE(8-13,7-8)> #<Edit REPLACE(17-19,12-14)> #<Edit REPLACE(29-31,24-25)> #<Edit DELETE(41-44,35-35)> #<Edit REPLACE(45-56,36-41)> #<Edit REPLACE(57-64,42-46)> #<Edit REPLACE(65-68,47-50)> #<Edit REPLACE(69-73,51-55)> #<Edit REPLACE(74-75,56-57)> #<Edit REPLACE(76-133,58-62)> #<Edit REPLACE(135-143,64-81)> #<Edit REPLACE(144-178,82-99)> #<Edit REPLACE(180-185,101-103)> #<Edit REPLACE(186-197,104-107)> #<Edit REPLACE(198-293,108-115)>] [#<Edit DELETE(0-79,0-0)>] [#<Edit REPLACE(8-9,8-9)> #<Edit REPLACE(60-70,60-66)>] [#<Edit REPLACE(16-17,16-17)>] [#<Edit DELETE(3-4,3-3)> #<Edit REPLACE(50-51,49-50)>] [#<Edit DELETE(6-38,6-6)> #<Edit REPLACE(43-46,11-14)>] [#<Edit DELETE(5-36,5-5)> #<Edit DELETE(37-41,6-6)> #<Edit DELETE(43-44,8-8)>]
   )

  {:change-type "DELETE", :score 0, :old-path "src/gita/api/seek.clj", :new-mode "0", :tree-filter-marks 0, :new-path "/dev/null", :old-id "173ce2e785437f37273a3ba58be2f30819ae4b48", :old-mode "100644", :new-id "0000000000000000000000000000000000000000"}) 
