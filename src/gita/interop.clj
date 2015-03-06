(ns gita.interop
  (:require [gita.macros :as macros]
            [gita.interop [common :as common]
                          dir-cache dir-cache-entry enum
                          file file-mode git object-id
                          person-id repository
                          rev-commit rev-walk status]))

(defn meta-object [cls]
  (common/-meta-object cls))

(defn to-data [obj]
  (common/-to-data obj))

(defn from-data [obj type]
  (common/-from-data obj type))
