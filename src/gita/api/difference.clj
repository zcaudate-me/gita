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

(defn git-diff
  [^Repository repo old new]
  (-> (Git. repo)
      (.diff)
      (.setOldTree (repository/tree-parser repo old))
      (.setNewTree (repository/tree-parser repo new))
      (.call)))

(defn list-difference
  [^Repository repo old new]
  (->> (git-diff repo old new)
       (map (partial format-entry repo))))

(defn list-file-changes
  [^Repository repo old new]
  (->> (git-diff repo old new)
       (map (fn [entry]
              {:path (.getNewPath entry) :type (enum->keyword (.getChangeType entry))}))
       (filter (fn [{:keys [type]}] (#{:add :modify :copy} type)))))

(comment
  (list-file-changes
   (repository/repository)
   {:commit "HEAD~3"}
   {})
  
  (first (list-difference
          (repository/repository)
          {:commit "HEAD~3"}
               {}
               
               
               ))

  
  
  
  
  
  

  ) 
