(ns gita.interop
  (:require [gita.protocol :as protocol]))

(defn to-data [obj]
  (protocol/-to-data obj))

(defn from-data [data type]
  (protocol/-from-data data type))
