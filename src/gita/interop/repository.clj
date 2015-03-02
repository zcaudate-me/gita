(ns gita.interop.repository
  (:import org.eclipse.jgit.lib.Repository
           org.eclipse.jgit.internal.storage.file.FileRepository))

(defn to-data [repo]
  (-> repo (.getDirectory) (.getAbsolutePath)))

(defn from-data [path _]
  (FileRepository. path))

(def meta-object
  {:class     Repository
   :types     #{String}
   :to-data   to-data
   :from-data from-data})

(defmethod print-method Repository
  [v ^java.io.Writer w]
  (.write w (str "#repository::" (to-data v))))
