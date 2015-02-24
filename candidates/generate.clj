(ns gita.generate
  (:require [vinyasa.maven.jar :as jar]
            [clojure.java.io :as io]))

(defn class-list
  ([coordinate]
    (let [path (jar/maven-file coordinate)
          entries (-> (io/file path)
                      (java.util.jar.JarFile.)
                      (.entries)
                      (iterator-seq))]
      (->> entries
           (map (fn [entry] (.getName entry)))
           (filter (fn [filename] (.endsWith filename ".class")))
           (map (fn [filename]
                  (-> filename
                      (.replaceAll "/" ".")
                      (.replaceAll "\\.class$" "")))))))
  ([coordinate package]
   (let [pattern (cond (or (string? package) (symbol? package))
                       (-> (str package)
                           (.replace "." "\\.")
                           (str "\\.[^\\.]+$")
                           (re-pattern))

                       (instance? java.util.regex.Pattern package)
                       package)]
     (->> (class-list coordinate)
          (filter (fn [cls] (re-find pattern cls)))))))


(defn command-list [])

(comment
  (class-list '[org.clojure/clojure "1.6.0"] 'clojure.core)

  (class-list '[org.clojure/clojure "1.6.0"] #"clojure.lang.[^$]+$"))
