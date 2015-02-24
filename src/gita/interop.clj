(ns gita.interop
  (:require [gita.protocol :as protocol]
            [gita.interop dir-cache enum object-id repository status]))

(defn to-data [obj]
  (protocol/-to-data obj))
	
(defn data-types [obj]
  (protocol/-data-types obj))

(defn from-data [data type]
  (protocol/-from-data data type))
