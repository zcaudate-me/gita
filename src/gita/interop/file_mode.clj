(ns gita.interop.file-mode
  (:require [gita.interop.common :as common])
  (:import org.eclipse.jgit.lib.FileMode))

(defmethod common/-meta-object FileMode
  [type]
  {:class     FileMode
   :types     #{String}
   :to-data   common/-to-data
   :from-data common/-from-data})

(extend-protocol common/IData
  FileMode
  (-to-data [mode]
    (.toString mode)))

(defmethod common/-from-data FileMode
  [data _]
  (FileMode/fromBits (read-string (str "8r" data))))

(defmethod print-method FileMode
  [v ^java.io.Writer w]
  (.write w (str "#mode::" (common/-to-data v))))
