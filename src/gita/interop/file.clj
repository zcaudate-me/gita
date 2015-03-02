(ns gita.interop.file
  (:require [clojure.java.io :as io])
  (:import java.io.File))

(defn to-data [file]
  (.getAbsolutePath file))

(defn from-data [path _]
  (io/file path))

(def meta-object
  {:class     File
   :types     #{String}
   :to-data   to-data
   :from-data from-data})

(defmethod print-method File
  [v ^java.io.Writer w]
  (.write w (str "#file::" (to-data v))))
