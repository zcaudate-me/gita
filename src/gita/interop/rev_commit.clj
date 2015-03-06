(ns gita.interop.rev-commit
  (:require [gita.interop.common :as common]
            [gita.interop.util :as util])
  (:import org.eclipse.jgit.revwalk.RevCommit))

(defmethod common/-meta-object RevCommit
  [type]
  {:class     RevCommit
   :types     #{java.util.Map}
   :to-data   common/-to-data})

(extend-protocol common/IData
  RevCommit
  (-to-data [entry]
    (-> entry
        (util/object-data common/-to-data)
        (dissoc :parents :parent-count :raw-buffer
                :short-message :commiter-ident :encoding))))

(defmethod print-method RevCommit
  [v ^java.io.Writer w]
  (.write w (str "#commit::" (common/-to-data v))))
