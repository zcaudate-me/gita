(ns gita.interop.repository
  (:require [gita.interop.common :as common])
  (:import org.eclipse.jgit.lib.Repository
           org.eclipse.jgit.internal.storage.file.FileRepository))

(defmethod common/-meta-object Repository
  [type]
  {:class     Repository
   :types     #{String}
   :to-data   common/-to-data
   :from-data common/-from-data})

(extend-protocol common/IData
  Repository
  (-to-data [repo]
    (-> repo (.getDirectory) (.getAbsolutePath))))

(defmethod common/-from-data Repository
  [path _]
  (FileRepository. path))

(defmethod print-method Repository
  [v ^java.io.Writer w]
  (.write w (str "#repository::" (common/-to-data v))))
