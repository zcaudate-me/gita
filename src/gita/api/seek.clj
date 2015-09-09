(ns gita.api.seek
  (:require [gita.api.repository :as repository]
            [hara.time :as time])
  (:import org.eclipse.jgit.treewalk.CanonicalTreeParser
           org.eclipse.jgit.lib.Constants))


(defprotocol Filesystem
  (list-files [fs])
  (get-file [fs path]))


(extend-protocol Filesystem
  org.eclipse.jgit.internal.storage.file.FileRepository
  (list-files [fs])
  (get-file [fs path]))

(defn resolve-id [repo id]
    (cond (string? id) id
          (.resolve repo id)

          (time/time? id)
          (time/to-long id)
          
          ))

(defn seek [repo options])

(comment
  (.%> org.eclipse.jgit.internal.storage.file.FileRepository)
  [org.eclipse.jgit.internal.storage.file.FileRepository [org.eclipse.jgit.lib.Repository #{}] [java.lang.Object #{java.lang.AutoCloseable}]]
  (repo)
  
  (type repo)

  (.? (repository/repository) :name)
  (.* (repository/repository) :name)
  ("clone" "close" "create" "detectIndexChanges" "doClose" "equals" "finalize" "fireEvent" "fs" "getAdditionalHaves" "getAllRefs" "getAllRefsByPeeledObjectId" "getBranch" "getClass" "getConfig" "getDirectory" "getFS" "getFullBranch" "getIndexFile" "getListenerList" "getObjectDatabase" "getObjectsDirectory" "getRef" "getRefDatabase" "getReflogReader" "getRemoteName" "getRemoteNames" "getRepositoryState" "getTags" "getWorkTree" "gitDir" "hasObject" "hashCode" "incrementOpen" "indexFile" "isBare" "loadRepoConfig" "loadSystemConfig" "loadUserConfig" "lockDirCache" "myListeners" "newObjectInserter" "newObjectReader" "notify" "notifyAll" "notifyIndexChanged" "objectDatabase" "open" "openPack" "parseSimple" "peel" "readCherryPickHead" "readCommitEditMsg" "readCommitMsgFile" "readDirCache" "readGitDirectoryFile" "readMergeCommitMsg" "readMergeHeads" "readOrigHead" "readRebaseTodo" "readRevertHead" "readSquashCommitMsg" "refs" "renameRef" "repoConfig" "resolve" "resolveAbbreviation" "resolveReflog" "resolveReflogCheckout" "resolveSimple" "scanForRepoChanges" "shortenRemoteBranchName" "simplify" "snapshot" "systemConfig" "toString" "updateRef" "useCnt" "userConfig" "wait" "workTree" "writeCherryPickHead" "writeCommitEditMsg" "writeCommitMsg" "writeHeadsFile" "writeMergeCommitMsg" "writeMergeHeads" "writeOrigHead" "writeRebaseTodoFile" "writeRevertHead" "writeSquashCommitMsg")
    
  (org.eclipse.jgit.lib.ObjectId/fromString "15bedfb280443397dbd303c73899f85a524f3881")
  
  (.resolve (repository/repository) "origin/gh-pages")

  (.resolve (repository/repository) org.eclipse.jgit.lib.Constants/MASTER)
  #id "e6a2401f173976c465fe49fd3480d8dd7954ba05"

  (.resolve (repository/repository) "gh-pages")
  #id "1cf1c3b441ca3fcc8eb363f663d1da3e54ff0a01"
  
  #id "15bedfb280443397dbd303c73899f85a524f3881"
  (.resolve (repository/repository) "")

  (def repo (repository/repository))
  (list-files repo)

  (seek repo
        {:branch "hello"
         :at #inst "2015-09-06T07:13:08.544-00:00"})

  
  (type Constants/HEAD)
  
  
  (version repo :branch)

  (explore repo :ex)
  (explore repo :in)

  ;; get the state of the repo at a particular time
  ;; get the state of the repo for a particular branch
  ;; get all the branches of a repo at a particular time

  ;; get a file-seq of files in the repo
  ;;

  ()

  (recall repo :at :branch )
  )
