(ns gita.interop.git
  (:require [gita.interop.common :as common])
  (:import org.eclipse.jgit.api.Git
           org.eclipse.jgit.lib.Repository))

(defmethod common/-meta-object Git
  [type]
  {:class     Git
   :types     #{String}
   :to-data   common/-to-data
   :from-data common/-from-data})

(extend-protocol common/IData
  Git
  (-to-data [git]
    (-> git (.getRepository) common/-to-data)))

(defmethod common/-from-data Git
  [path _]
  (Git. (common/-from-data path Repository)))

(defmethod print-method Git
  [v ^java.io.Writer w]
  (.write w (str "#git::" (common/-to-data v))))
