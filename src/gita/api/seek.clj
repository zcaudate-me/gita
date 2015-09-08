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

  (org.eclipse.jgit.lib.ObjectId/fromString "15bedfb280443397dbd303c73899f85a524f3881")
  
  (.resolve (repository/repository) "origin/master")
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
