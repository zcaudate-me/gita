(ns gita.interop.file-mode
  (:import org.eclipse.jgit.lib.FileMode))

(defn to-data [mode]
  (.toString mode))

(defn from-data [data _]
  (FileMode/fromBits (read-string (str "8r" data))))

(def meta-object
  {:class     FileMode
   :types     #{String}
   :to-data   to-data
   :from-data from-data})

(defmethod print-method FileMode
  [v ^java.io.Writer w]
  (.write w (str "#mode::" (to-data v))))
