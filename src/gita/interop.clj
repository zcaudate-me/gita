(ns gita.interop
  (:require [gita.protocol :as protocol]
            [gita.macros :as macros]
            [gita.interop.dir-cache :as dir-cache]
            [gita.interop.dir-cache-entry :as dir-cache-entry]
            [gita.interop.enum :as enum]
            [gita.interop.file :as file]
            [gita.interop.file-mode :as file-mode]
            [gita.interop.git :as git]
            [gita.interop.object-id :as object-id]
            [gita.interop.repository :as repository]
            [gita.interop.status :as status]
            [hara.class.inheritance :as inheritance])
  (:import org.eclipse.jgit.dircache.DirCache
           org.eclipse.jgit.dircache.DirCacheEntry
           java.io.File
           org.eclipse.jgit.lib.FileMode
           org.eclipse.jgit.api.Git
           org.eclipse.jgit.lib.ObjectId
           org.eclipse.jgit.lib.Repository
           org.eclipse.jgit.api.Status))

(defn meta-object [cls]
  (condp = cls
    DirCache        dir-cache/meta-object
    DirCacheEntry   dir-cache-entry/meta-object
    File            file/meta-object
    FileMode        file-mode/meta-object
    Git             git/meta-object
    ObjectId        object-id/meta-object
    Repository      repository/meta-object
    Status          status/meta-object
    (cond (-> (inheritance/ancestor-list cls)
              (set)
              (get Enum))
          enum/meta-object
          :else
          {:class cls :types #{}})))

(defprotocol IData
  (to-data [obj]))

(extend-protocol IData
  Object
  (to-data [obj] obj))

(macros/extend-namespaced
 [IData]

 Enum enum
 DirCache dir-cache
 DirCacheEntry dir-cache-entry
 File file
 FileMode file-mode
 Git git
 ObjectId object-id
 Repository repository
 Status status)

(defmulti from-data (fn [obj type] type))

(macros/extend-namespaced-multi
 (from-data [data type])

 Enum enum
 File file
 FileMode file-mode
 Git git
 ObjectId object-id
 Repository repository)
