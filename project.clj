(defproject im.chit/gita "0.1.0"
  :description "git on song"
  :url "https://www.github.com/zcaudate/gita"
  :license {:name "The MIT License"
            :url "http://http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [im.chit/hara.reflect "2.1.10"]
                 [im.chit/hara.string.case "2.1.10"]
                 [im.chit/vinyasa.maven "0.3.3"]
                 [org.eclipse.jgit "3.6.2.201501210735-r"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]]}})