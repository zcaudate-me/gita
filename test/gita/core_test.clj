(ns gita.core-test
  (:require [clojure.test :refer :all]
            [gita.core :refer :all]))

(comment
  (first (git :log ))
    
  (git :status)
  
  (git :checkout :name "gh-pages")

  
  (git)
  [:add :apply :archive :blame :branch :checkout :cherry :clean :clone :commit :describe :diff :fetch :gc :init :log :ls :merge :name :notes :pull :push :rebase :reflog :reset :revert :rm :stash :status :submodule :tag]

  (git :init :directory "/tmp/gita-example")

  (git :cd "/tmp/gita-example")
  (do (spit "/tmp/gita-example/hello.txt" "hello")
      (spit "/tmp/gita-example/world.txt" "world")
      (spit "/tmp/gita-example/again.txt" "again"))

  (git :add :filepattern ["hello.txt"])
  => {"hello.txt" #{:merged}}

  (git :commit :message "Added Hello.txt")
  => {:commit-time 1425683330,
      :name "9f1177ad928d7dea2afedd58b1fb7192b3523a6c",
      :author-ident {:email-address "z@caudate.me",
                     :name "Chris Zheng",
                     :time-zone-offset 330,
                     :when #inst "2015-03-06T23:08:50.000-00:00"},
      :full-message "Added Hello.txt"}

  (git :log)
  => [{:commit-time 1425683330,
      :name "9f1177ad928d7dea2afedd58b1fb7192b3523a6c",
      :author-ident {:email-address "z@caudate.me",
                     :name "Chris Zheng",
                     :time-zone-offset 330,
                     :when #inst "2015-03-06T23:08:50.000-00:00"},
       :full-message "Added Hello.txt"}]

  ;; Oops.. I made an error:
  (git :commit :message "Added `hello.txt`" :amend true)
  => {:commit-time 1425683376,
      :name "f21fe52cdc3511918b7d52e43f909dbe3c380159",
      :author-ident {:email-address "z@caudate.me",
                     :name "Chris Zheng",
                     :time-zone-offset 330,
                     :when #inst "2015-03-06T23:08:50.000-00:00"},
      :full-message "Added `hello.txt`"}

  (git :add :filepattern ["."])
  => {"again.txt" #{:merged},
      "hello.txt" #{:merged},
      "world.txt" #{:merged}}

  (git :commit :message "Added `world.txt` and `again.txt`")
  => {:commit-time 1425683590,
      :name "2aad32d04470f118c1c891e163290433d70bbd21",
      :author-ident {:email-address "z@caudate.me",
                     :name "Chris Zheng",
                     :time-zone-offset 330,
                     :when #inst "2015-03-06T23:13:10.000-00:00"},
      :full-message "Added `world.txt` and `again.txt`"}

  (mapv :name (git :log))
  => ["2aad32d04470f118c1c891e163290433d70bbd21"
      "f21fe52cdc3511918b7d52e43f909dbe3c380159"]

  (git :rm :filepattern ["hello.txt" "world.txt"])
  => {"again.txt" #{:merged}}

  (git :commit :message "Leave `again.txt` as the only file")
  (mapv :name (git :log))
  => ["9fb08f2b6d10ae1ec8cf15eb81ac56edd504160f"
      "2aad32d04470f118c1c891e163290433d70bbd21"
      "f21fe52cdc3511918b7d52e43f909dbe3c380159"]

  (git :status)
  => {:clean? true, :uncommitted-changes! false}

  (def status-obj (git :status :&))
  (type status-obj)
  => org.eclipse.jgit.api.Status


  (require '[hara.reflect :as reflect])
  (reflect/query-class status-obj [:name])
  => ("clean" "diff" "getAdded" "getChanged" "getConflicting" "getConflictingStageState" "getIgnoredNotInIndex" "getMissing" "getModified" "getRemoved" "getUncommittedChanges" "getUntracked" "getUntrackedFolders" "hasUncommittedChanges" "isClean" "new")

  (git :diff)
  (type (git :init :directory "/tmp/git-example" :&))
  org.eclipse.jgit.api.Git


  )

(comment
  (git)
  (git :cd)
  (git :pwd)
  (git :init :?)
  ;; => {:git-dir java.lang.String, :directory java.lang.String, :bare boolean}

  (git :add :?)
  (git :rm :?)
  (def res (git :stash :create))
  (def author (.getAuthorIdent res))
  (type res)
  () (util/object-methods res)


  (git :pwd)
  "/tmp/gita-example"
  "/Users/chris/Development/chit/gita"


  => "/tmp/gita-example/.git"
  (do (git :init :directory "/tmp/gita-example")
      (git :cd "/tmp/gita-example")
      (spit "/tmp/gita-example/hello.txt" "hello there")
      (git :add :filepattern ["."])
      (git :commit :message (str (rand-int 1000) " - basic commit"))
      (spit "/tmp/gita-example/hello.txt" "hello world")
      (git :stash :create)
      (spit "/tmp/gita-example/hello.txt" "hello foo")
      (git :stash :create))
  
  (git :stash :list)
  (count (git :log))


  (git :cd "/tmp/gita-example1")

  (spit "/tmp/gita-example/hello.note" "hello there")
  (spit "/tmp/gita-example/hello.txt" "hello there")
  (git :status)
  (git :add :filepattern ["."])
  (iterator-seq (.iterator (git :log)))

  (type (.getAuthor (.next (git :log)))

        (type (.getEncoding (.next (git :log))))
        sun.nio.cs.UTF_8
        (-> (git :log) (.iterator) (iterator-seq))

        (.getAuthorIdent (.next (git :log))))


  (git :add :?)
  (git :cd)
  (git :push :remote "git@github.com:zcaudate/gita.git")
  (def res (git :pull :&))

  (interop/to-data res)

  (git :init :?)
  (git :status)


  (spit "/tmp/gita-example/hello.txt" "hello there")
  (git :add :filepattern ["."])
  (git :commit :message "basic commit" :&)
  (git :rm :help)
  (git :status)
  (def res )
  (util/object-methods res)

  (git "/tmp/gita/init" :status)
  (git "." :status)
  (git :branch :create))
