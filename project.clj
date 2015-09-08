(defproject im.chit/gita "0.1.8"
  :description "git on song"
  :url "https://www.github.com/zcaudate/gita"
  :license {:name "The MIT License"
            :url "http://http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [im.chit/hara.object "2.2.11"]
                 [im.chit/hara.reflect "2.2.11"]
                 [org.eclipse.jgit "4.0.1.201506240215-r"]]
  :global-vars {*warn-on-reflection* true}
  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]]}})
