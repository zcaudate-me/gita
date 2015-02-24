(ns gita.interop.repository
  (:require [gita.protocol :as protocol])
  (:import org.eclipse.jgit.lib.Repository
           org.eclipse.jgit.internal.storage.file.FileRepository))

(extend-protocol protocol/IData
  Repository
  (-to-data [repo]
    (-> repo (.getDirectory) (.getAbsolutePath)))
  (-data-types [_] #{String}))

(defmethod protocol/-from-data Repository
  [^String path _] (FileRepository. path))
