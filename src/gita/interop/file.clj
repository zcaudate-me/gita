(ns gita.interop.file
  (:require [gita.interop.common :as common]
            [clojure.java.io :as io])
  (:import java.io.File))

(defmethod common/-meta-object File
  [type]
  {:class     File
   :types     #{String}
   :to-data   common/-to-data
   :from-data common/-from-data})

(extend-protocol common/IData
  File
  (-to-data [file]
    (.getPath file)))

(defmethod common/-from-data File
  [path _]
  (io/file path))

(defmethod print-method File
  [v ^java.io.Writer w]
  (.write w (str "<" (common/-to-data v) ">")))
