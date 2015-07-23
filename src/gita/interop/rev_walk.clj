(ns gita.interop.rev-walk
  (:require [hara.protocol.data :as data]
            [hara.object :as object])
  (:import org.eclipse.jgit.revwalk.RevWalk))

(defmethod data/-meta-object RevWalk
  [type]
  {:class     RevWalk
   :types     #{clojure.lang.PersistentVector}
   :to-data   data/-to-data})

(extend-protocol data/IData
  RevWalk
  (-to-data [walk]
    (->> walk (.iterator) object/to-data)))

(defmethod print-method RevWalk
  [v ^java.io.Writer w]
  (.write w (str "#commits::" (data/-to-data v))))
