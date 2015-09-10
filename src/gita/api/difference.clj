(ns gita.api.difference
  (:require [gita.api.repository :as repository]
            [clojure.java.io :as io]
            [hara.object :as object])
  (:import [org.eclipse.jgit.diff HistogramDiff RawTextComparator RawText
            DiffEntry DiffEntry$ChangeType Edit EditList]
           [org.eclipse.jgit.lib Repository Constants AbbreviatedObjectId]
           org.eclipse.jgit.api.Git))

(defonce null-id "0000000000000000000000000000000000000000")

(defn enum->keyword [enum]
  (keyword (.toLowerCase (str enum))))

(defrecord Change [])

(defmethod print-method Change
  [v ^java.io.Writer w]
  (let [shorthand  (fn [v] [(-> v :lines :start) (-> v :lines :end)])]
    (.write w (str "#" (name (:type v)) " "
                   (-> (into {} v)
                       (dissoc :type)
                       (update-in [:old] shorthand)
                       (update-in [:new] shorthand))))))

(defn retrieve-text
  [^Repository repo ^AbbreviatedObjectId id start end]
  (cond (= (.name id) null-id) []

        (= start end) []

        :else
        (let [lines (-> repo
                        (.open (.toObjectId id) Constants/OBJ_BLOB)
                        (.openStream)
                        (io/reader)
                        (line-seq)
                        vec)
              cnt (count lines)]
          (->> lines
               (map-indexed (fn [i line]
                              [(inc i) line]))
               (keep (fn [[i line]]
                       (if (< start i (inc end))
                         (if (= cnt i)
                           line
                           (str line "\\n")))))))))

(defn edit->change
  [^Edit edit]   
  (map->Change {:type (enum->keyword (.getType edit))
                :old {:lines {:start  (.getBeginA edit)
                              :end    (.getEndA edit)
                              :length (.getLengthA edit)}}
                :new {:lines {:start  (.getBeginB edit)
                              :end    (.getEndB edit)
                              :length (.getLengthB edit)}}}))

(defn format-change
  [^Repository repo ^Edit edit old-id new-id]
  (let [retrieve (fn [{:keys [lines] :as entry} id]
                   (assoc entry :text (retrieve-text repo id (:start lines) (:end lines))))]
    (-> (edit->change edit)
        (update-in [:old] retrieve old-id)
        (update-in [:new] retrieve new-id))))

(defn format-changes
  [^Repository repo entry old-id new-id]
  (let [get-text (fn [^AbbreviatedObjectId id]
                   (if (and id (not= (.name id) null-id))
                     (-> repo
                         (.open (.toObjectId id) Constants/OBJ_BLOB)
                         (.getCachedBytes)
                         (RawText.))
                     RawText/EMPTY_TEXT))
        old-text (get-text old-id)
        new-text (get-text new-id)
        changes  (-> (HistogramDiff.)
                     (.diff RawTextComparator/DEFAULT old-text new-text))]
    (mapv #(format-change repo % old-id new-id) changes)))

(defrecord Entry [])

(defmethod print-method Entry
  [v ^java.io.Writer w]
  (.write w (str "#" (name (:type v)) " " (into {} (dissoc v :type)))))

(defn format-entry
  [^Repository repo ^DiffEntry entry]
  (let [type    (enum->keyword (.getChangeType entry))
        changes (format-changes repo entry (.getOldId entry) (.getNewId entry))
        entry   (-> (object/to-data entry)
                    (assoc :type type :changes changes)
                    (dissoc :change-type :tree-filter-marks :score :new-mode :old-mode)
                    (map->Entry))]
    (case type
      :add    (dissoc entry :old-id :old-path)
      :delete (dissoc entry :new-id :new-path)
      entry)))

(defn list-difference
  ([^Repository repo old new]
   (-> (Git. repo)
       (.diff)
       (.setOldTree (repository/tree-parser repo old))
       (.setNewTree (repository/tree-parser repo new))
       (.call)
       (->> (map (partial format-entry repo))))))



(comment

  (first (list-difference
               (repository/repository)
               {:commit "HEAD~3"}
               {}
               
               
               ))

  
  
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
