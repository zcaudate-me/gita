(ns gita.interop.string-like
  (:require [clojure.java.io :as io]
            [hara.object :as object]))

(object/extend-stringlike

 java.io.File
 {:tag   "//"
  :to     .getPath
  :from   (fn [path _] (io/file path))}

 org.eclipse.jgit.lib.FileMode
 {:tag   "mode"
  :to     .toString
  :from   (fn [data _]
            (org.eclipse.jgit.lib.FileMode/fromBits
             (read-string (str "8r" data))))}

 org.eclipse.jgit.api.Git
 {:tag   "git"
  :to     (fn [^org.eclipse.jgit.api.Git repo]
            (-> repo (.getRepository) object/to-data))
  :from   (fn [path _]
            (-> path
                (object/from-data org.eclipse.jgit.lib.Repository)
                (org.eclipse.jgit.api.Git. )))}

 org.eclipse.jgit.lib.AnyObjectId
 {:tag   "id"
  :to     .getName
  :from   (fn [data _]
            (org.eclipse.jgit.lib.ObjectId/fromString data))}

 org.eclipse.jgit.lib.AbbreviatedObjectId
 {:tag   "id"
  :to     .name
  :from   (fn [data _]
            (org.eclipse.jgit.lib.AbbreviatedObjectId/fromString data))}

 org.eclipse.jgit.lib.Repository
 {:tag   "repository"
  :to     (fn [^org.eclipse.jgit.lib.Repository repo]
            (-> repo (.getDirectory) object/to-data))
  :from   (fn [^String path _]
            (org.eclipse.jgit.internal.storage.file.FileRepository. path))}

 org.eclipse.jgit.transport.URIish
 {:tag   "url"
  :to     str
  :from   (fn [^String path _]
            (org.eclipse.jgit.transport.URIish. path))})
