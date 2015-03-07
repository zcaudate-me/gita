(ns gita.interop.string-like
  (:require [gita.interop.common :as common]
            [gita.interop.util :as util]
            [clojure.java.io :as io]))

(defmacro extend-stringlike-class [cls opts]
  `(vector
    (defmethod common/-meta-object ~cls
      [type#]
      {:class     type#
       :types     #{String}
       :to-data   common/-to-data
       :from-data common/-from-data})

    (extend-protocol common/IData
      ~cls
      (-to-data [obj#]
        (~(:to opts) obj#)))

    (defmethod common/-from-data ~cls
      [data# type#]
      (~(:from opts) data# type#))

    (defmethod print-method ~cls
      [v# ^java.io.Writer w#]
      (.write w# (str "#" ~(:slug opts) "::" (common/-to-data v#))))))

(defmacro extend-stringlike [& {:as classes}]
  `(vector ~@(map (fn [[cls opts]]
                    `(extend-stringlike-class ~cls ~opts))
                  classes)))

(extend-stringlike

 java.io.File
 {:slug   "//"
  :to     .getPath
  :from   (fn [path _] (io/file path))}

 org.eclipse.jgit.lib.FileMode
 {:slug   "mode"
  :to     .toString
  :from   (fn [data _]
            (org.eclipse.jgit.lib.FileMode/fromBits
             (read-string (str "8r" data))))}

 org.eclipse.jgit.api.Git
 {:slug   "git"
  :to     (fn [^org.eclipse.jgit.api.Git repo]
            (-> repo (.getRepository) common/-to-data))
  :from   (fn [path _]
            (-> path
                (common/-from-data org.eclipse.jgit.lib.Repository)
                (org.eclipse.jgit.api.Git. )))}

 org.eclipse.jgit.lib.AnyObjectId
 {:slug   "id"
  :to     .getName
  :from   (fn [data _]
            (org.eclipse.jgit.lib.ObjectId/fromString data))}

 org.eclipse.jgit.lib.AbbreviatedObjectId
 {:slug   "id"
  :to     .name
  :from   (fn [data _]
            (org.eclipse.jgit.lib.AbbreviatedObjectId/fromString data))}

 org.eclipse.jgit.lib.Repository
 {:slug   "repository"
  :to     (fn [^org.eclipse.jgit.lib.Repository repo]
            (-> repo (.getDirectory) common/-to-data))
  :from   (fn [^String path _]
            (org.eclipse.jgit.internal.storage.file.FileRepository. path))}

 org.eclipse.jgit.transport.URIish
 {:slug   "url"
  :to     str
  :from   (fn [^String path _]
            (org.eclipse.jgit.transport.URIish. path))})
