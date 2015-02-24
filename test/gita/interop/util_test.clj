(ns gita.interop.util-test
  (:require [gita.interop.util :refer :all]
            [midje.sweet :refer :all]))

(fact "java->clojure"
 (java->clojure "isAppleCrumble")
 => "apple-crumble?"

 (java->clojure "hasAppleCrumble")
 => "apple-crumble?"

 (java->clojure "getAppleCrumble")
 => "apple-crumble")
