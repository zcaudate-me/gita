(ns gita.interop.git
  (:require [gita.interop.repository :as repository])
  (:import org.eclipse.jgit.api.Git))

(defn to-data [git]
  (-> git (.getRepository) (repository/to-data)))

(defn from-data [path _]
  (Git. (repository/from-data path nil)))

(def meta-object
  {:class     Git
   :types     #{String}
   :to-data   to-data
   :from-data from-data})

(defmethod print-method Git
  [v ^java.io.Writer w]
  (.write w (str "#git::" (to-data v))))
