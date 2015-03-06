(ns gita.interop.person-id
  (:require [hara.reflect :as reflect]
            [gita.interop.common :as common]
            [gita.interop.util :as util])
  (:import org.eclipse.jgit.lib.PersonIdent))

(defmethod common/-meta-object PersonIdent
  [type]
  {:class     PersonIdent
   :types     #{java.util.Map}
   :to-data   common/-to-data})

(extend-protocol common/IData
  PersonIdent
  (-to-data [entry]
    (-> (util/object-data entry common/-to-data)
        (dissoc :time-zone))))

(defmethod print-method PersonIdent
  [v ^java.io.Writer w]
  (.write w (str "#person::" (common/-to-data v))))
