(defproject im.chit/gita "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [im.chit/hara.reflect "2.1.10"]
                 [im.chit/hara.string.case "2.1.10"]
                 [im.chit/vinyasa.maven "0.3.3"]
                 [org.eclipse.jgit "3.6.2.201501210735-r"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]]}})
