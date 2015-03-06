(ns gita.interop.rev-walk
  (:require [gita.interop.common :as common])
  (:import org.eclipse.jgit.revwalk.RevWalk))

(defmethod common/-meta-object RevWalk
  [type]
  {:class     RevWalk
   :types     #{clojure.lang.PersistentVector}
   :to-data   common/-to-data})

(extend-protocol common/IData
  RevWalk
  (-to-data [walk]
    (->> walk (.iterator) iterator-seq (map common/-to-data) vec)))

(defmethod print-method RevWalk
  [v ^java.io.Writer w]
  (.write w (str "#commits::" (common/-to-data v))))
