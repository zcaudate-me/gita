(ns gita.interop
  (:require [gita.macros :as macros]
            [gita.interop
             [common :as common]
             map-like string-like
             dir-cache enum file-snapshot
             rev-walk status]))

(defn meta-object [cls]
  (common/-meta-object cls))

(defn to-data [obj]
  (common/to-data obj))

(defn from-data [obj type]
  (common/-from-data obj type))
