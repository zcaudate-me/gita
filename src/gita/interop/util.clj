(ns gita.interop.util
  (:require [hara.string.case :as case]))

(defn java->clojure [name]
  (let [nname (cond (re-find #"^get.+" name)
                    (subs name 3)

                    (re-find #"^is.+" name)
                    (str (subs name 2) "?")

                    (re-find #"^has.+" name)
                    (str (subs name 3) "?")

                    :else name)]
    (case/spear-case nname)))
