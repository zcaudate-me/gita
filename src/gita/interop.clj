(ns gita.interop
  (:require [gita.protocol :as protocol]
            [gita.macros :as macros]
            [gita.interop.enum :as enum]
            [gita.interop.dir-cache :as dir-cache]
            [gita.interop.dir-cache-entry :as dir-cache-entry]
            [gita.interop.file :as file]
            [gita.interop.git :as git]
            [gita.interop.object-id :as object-id]
            [gita.interop.repository :as repository]
            [gita.interop.status :as status]))

(defprotocol IData
  (to-data [obj])
  (meta-object [obj key]))

(macros/extend-namespaced
 [IData]
 ;;{meta-object ([_] %/meta-object)}

 Enum enum
 org.eclipse.jgit.dircache.DirCache dir-cache
 org.eclipse.jgit.dircache.DirCacheEntry dir-cache-entry
 java.io.File file
 org.eclipse.jgit.api.Git git
 org.eclipse.jgit.lib.ObjectId object-id
 org.eclipse.jgit.lib.Repository repository
 org.eclipse.jgit.api.Status status)

(defmulti from-data (fn [obj type] type))

(macros/extend-namespaced-multi
 (from-data [data type])

 Enum enum
 java.io.File file
 org.eclipse.jgit.api.Git git
 org.eclipse.jgit.lib.ObjectId object-id
 org.eclipse.jgit.lib.Repository repository)
