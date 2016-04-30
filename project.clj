(defproject im.chit/gita "0.2.5"
  :description "git on song"
  :url "https://www.github.com/zcaudate/gita"
  :license {:name "The MIT License"
            :url "http://http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [im.chit/hara.object "2.3.3"]
                 [im.chit/hara.reflect "2.3.3"]
                 [org.eclipse.jgit "4.3.0.201604071810-r"]]
  ;;:global-vars {*warn-on-reflection* true}
  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]]}})
