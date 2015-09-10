# gita

[![Build Status](https://travis-ci.org/zcaudate/gita.png?branch=master)](https://travis-ci.org/zcaudate/gita)

the introspective [jgit](https://eclipse.org/jgit/) wrapper

     No one who does good work will ever come to a
     bad end, either here or in the world to come.
     
      - Krishna, Bhagavad Gita
  
## Overview

`gita` is a wrapper around the popular [jgit](https://eclipse.org/jgit/) project. An alternative library is [clj-jgit](https://github.com/clj-jgit/clj-jgit) and it does have alot more higher-level functions at the moment. However, due to the enormous amount of functionality around [git](http://www.git.org), it is very difficult to manually write a wrapper around the entire suite. So The novelty of [gita](https://www.github.com/zcaudate/gita) is that it uses reflection provided by [hara.reflect](https://www.github.com/zcaudate/hara) to generate the wrapper interface in such a way that the entire functionality of the main `org.eclipse.jgit.api.Git` class is accessible and usable in a clojure compatible convention.

The aims of this project are:

  - to have a git library for clojure that is simple and intuitive to use
  - to use less than 1000 lines of code
  - to use reflection, allowing for:
    - self-directed exploration of the library
    - auto-generation of the clojure interface
    - auto-coercion of string-like inputs (ids and files)
    - auto-conversion of outputs to simple clojure data
  - to learn more about git in the process

## Installation

Add to project.clj dependencies:

```clojure
[im.chit/gita "0.2.0"]
```

## Usage

There is only one function: `git`. It's usage is very much like how the command-line version is used:

```clojure
(use 'gita.core)

;; This should feel somewhat familiar:

(git)
;  Subtasks for git are:
;
;  [:add :apply :archive :blame :branch :checkout 
;   :cherry :clean :clone :commit :describe :diff 
;   :fetch :gc :init :log :ls :merge :name :notes 
;   :pull :push :rebase :reflog :reset :revert :rm 
;   :stash :status :submodule :tag]
;
```

#### absolute basics - :init, :status, :cd and :pwd

```clojure
;; So we can now initialise a directory
(git :init :directory "/tmp/gita-example")
=> "/tmp/gita-example/.git"

;; Lets check the status of our new repository
(git "/tmp/gita-example" :status)
=> {:clean? true, :uncommitted-changes! false}

;; Now the default directory is set and we don't
;; need to set it next time we call:
(git :status)
=> {:clean? true, :uncommitted-changes! false}

;; We can also use `:cd` to set the default directory
(git :cd "/tmp/gita-example")
(git :status)
=> {:clean? true, :uncommitted-changes! false}

;; To check the current working directory, use `:pwd`
(git :pwd)
=> "/tmp/gita-example"

;; We add a file and then check for the repository status 
(spit "/tmp/gita-example/hello.txt" "hello there")
(git :status)
=> {:clean? false, :uncommitted-changes! false, :untracked #{"hello.txt"}}
```

#### exploration and self-help - :? and :help

We can get help with a subcommand at anytime by using `:?` or `:help` after the first keyword. This will return a map with keys and the type of argument that it takes:

```clojure
;; Lets take a look at the `:init` subcommand:
(git :init :?) ;; using `:help` does the same thing
=> {:git-dir java.lang.String, 
    :directory java.lang.String, 
    :bare boolean}  
```

So we can use it in the following way to create a bare git repository:

```clojure
(git :init :directory "/tmp/new-bare-repo" :bare true)
=> "/tmp/new-bare-repo"
```

Lets take a look at the `:status` subcommand:

```clojure
(git :status :?)
=> {:working-tree-it   org.eclipse.jgit.treewalk.WorkingTreeIterator, 
    :progress-monitor  org.eclipse.jgit.lib.ProgressMonitor, 
    :ignore-submodules #{"NONE" "UNTRACKED" "DIRTY" "ALL"}, 
    :path              [java.lang.String]}
```

We can decode the representation of the options

- for `:working-tree-it` an input of type `org.eclipse.jgit.treewalk.WorkingTreeIterator` is needed
- for `:progress-monitor` an input of type `org.eclipse.jgit.lib.ProgressMonitor` is needed
- for `:ignore-submodules` an input of the following options #{"NONE" "UNTRACKED" "DIRTY" "ALL"} is needed
- for `:path` a string input or a vector of strings is needed

#### working locally - :add, :commit, :log and :rm

So we continue to add on our basic vocabulary for our git workflow. Using help, we can have a look at what options `:add` and `:commit` take:

```clojure
(git :add :?)
=> {:working-tree-iterator org.eclipse.jgit.treewalk.WorkingTreeIterator,
    :update boolean,
    :filepattern [java.lang.String]}

(git :commit :?)
=> {:reflog-comment java.lang.String,
    :only java.lang.String,
    :message java.lang.String,
    :insert-change-id boolean,
    :committer java.lang.String,
    :author java.lang.String,
    :amend boolean,
    :all boolean}
```

Lets do an example, with the `/tmp/gita-example` repository, lets 

```clojure
(git :init :directory "/tmp/gita-example")
(git :cd "/tmp/gita-example")
```

We start off by creating three files and then commiting just the first

```clojure
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
```

Feel free to use your shell to browse to `/tmp/gita-example` and run `git status`. Now we can check what we have committed using `:log`:

```clojure
(git :log)
=> [{:commit-time 1425683330,
    :name "9f1177ad928d7dea2afedd58b1fb7192b3523a6c",
    :author-ident {:email-address "z@caudate.me",
                   :name "Chris Zheng",
                   :time-zone-offset 330,
                   :when #inst "2015-03-06T23:08:50.000-00:00"},
     :full-message "Added Hello.txt"}]
```

Oops.. I made an error in the status message. The file should be in lowercase. We can fix that:

```clojure
(git :commit :message "Added `hello.txt`" :amend true)
=> {:commit-time 1425683376,
    :name "f21fe52cdc3511918b7d52e43f909dbe3c380159",
    :author-ident {:email-address "z@caudate.me",
                   :name "Chris Zheng",
                   :time-zone-offset 330,
                   :when #inst "2015-03-06T23:08:50.000-00:00"},
    :full-message "Added `hello.txt`"}
```

Now lets add all the files and commit, and check that we indeed have two commits: 

```clojure
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
```    

We can test out `:rm` for removing files. Feel free to run `(git :rm :?)` to see the inputs:

```clojure
(git :rm :filepattern ["hello.txt" "world.txt"])
=> {"again.txt" #{:merged}}

(git :commit :message "Leave `again.txt` as the only file")
(mapv :name (git :log))
=> ["9fb08f2b6d10ae1ec8cf15eb81ac56edd504160f"
    "2aad32d04470f118c1c891e163290433d70bbd21"
    "f21fe52cdc3511918b7d52e43f909dbe3c380159"]

;; Checking the status, it is clean
(git :status)
=> {:clean? true, :uncommitted-changes! false}
```

#### raw-objects - :&

When `:&` is used in the parameter, the raw result of the commant call is returned instead of being converted into a corresponding map/string. This is demonstrated in the example below:

```clojure
(type (git :init :directory "/tmp/git-example" :&))
=> org.eclipse.jgit.api.Git

(def status-obj (git :status :&))
(type status-obj)
=> org.eclipse.jgit.api.Status

;; Instead of the ugly <org.eclipse.jgit.api.Status@234234> notation, the 
;; object is prettied up for printing
(println status-obj)
=> #status::{:clean? true, :uncommitted-changes! false} 

(require '[hara.reflect :as reflect])
(reflect/query-class status-obj [:name])
=> ("clean" "diff" "getAdded" "getChanged" "getConflicting" 
    "getConflictingStageState" "getIgnoredNotInIndex" "getMissing" 
    "getModified" "getRemoved" "getUncommittedChanges" "getUntracked" 
    "getUntrackedFolders" "hasUncommittedChanges" "isClean" "new")
```

#### working locally - :branch, :checkout and :merge

Lets look at branching, running `(git :branch)` lists the are 4 subcommands:

```clojure
(git :branch)
=> #{:create :delete :rename :list}
```

Using `:list` the current branches that are avaliable:

```clojure
(git :branch :list)
=> [{:object-id "9fb08f2b6d10ae1ec8cf15eb81ac56edd504160f"
     :name "refs/heads/master"
     :peeled-object-id nil
     :storage "LOOSE"
     :peeled? false :symbolic? false
     :snap-shot {:last-modified 1425683834000
                 :last-read 1425701350639
                 :cannot-be-racily-clean true}}]
```

The options that come with the :branch :create subcommand are:

```clojure
(git :branch :create :?)
=> {:upstream-mode #{"SET_UPSTREAM" "NOTRACK" "TRACK"}
    :start-point java.lang.String
    :name java.lang.String
    :force boolean}
```

We can create a new branch, we name this "tito"

```clojure
(git :branch :create :name "tito")
=> {:object-id "9fb08f2b6d10ae1ec8cf15eb81ac56edd504160f"
    :name "refs/heads/tito"
    :peeled-object-id nil
    :storage "LOOSE"
    :peeled? false :symbolic? false
    :snap-shot {:last-modified 1425701503000
                :last-read 1425701503780
                :cannot-be-racily-clean false}}
```

We take a list of the git branches and then checkout a new branch named tito

```clojure
(map :name (git :branch :list))
=> ("refs/heads/master" "refs/heads/tito")

(:name (git :checkout :name "tito"))
=> "refs/heads/tito"
```

We change two files and look at the `:diff`

```clojure
(spit "/tmp/gita-example/again.txt" "bonjour")
(spit "/tmp/gita-example/world.txt" "world")
(git :diff)
=> [{:change-type "MODIFY",
     :score 0,
     :old-path "again.txt",
     :new-mode "100644",
     :tree-filter-marks 0,
     :new-path "again.txt",
     :old-id "d3dc34affe77fdb18e4beef9d9b3213d791358e5",
     :old-mode "100644",
     :new-id "80bc9f6d40eef64fbaa976981b6b2916f71c1558"}
    {:change-type "ADD",
     :score 0,
     :old-path "/dev/null",
     :new-mode "100644",
     :tree-filter-marks 0,
     :new-path "world.txt",
     :old-id "0000000000000000000000000000000000000000",
     :old-mode "0",
     :new-id "04fea06420ca60892f73becee3614f6d023a4b7f"}]
```

We can now commit our changes and merge the changes into master

```clojure
(do (git :add :filepattern ["."])
    (git :commit :message "Changed to french"))
=> {:commit-time 1425702855,
    :name "964555215c54203747d118a45bf2595bf01257d1" ;; Take a note of this ID
    :full-message "Changed to french"}


;; I haven't figured out how to just write something like `(git :merge "tito")` but we 
;; can use the id of the commit to do it:
(git :checkout :name "master")
(git :merge :include "964555215c54203747d118a45bf2595bf01257d1")
=> {:base {:commit-time 1425702855
           :name "9fb08f2b6d10ae1ec8cf15eb81ac56edd504160f"}
    :checkout-conflicts nil :conflicts nil
    :failing-paths nil :merge-status "Already-up-to-date"
    :merged-commits [....]
    :new-head {:commit-time 1425702855
               :name "964555215c54203747d118a45bf2595bf01257d1"
               :full-message "Changed to french"}}
```

PHEW. Lets keep going

#### working remotely - :pull, :push

I'm going to cheat and just use this on the gita repo, for effects only:

```clojure
(git :cd) ;; Goes to my default dir (the gita directory)

(git :pull) 
=> {:fetched-from "origin",
    :fetch-result {:advertised-refs [...], :messages "", :tracking-ref-updates [],
                  :uri "git@github.com:zcaudate/gita.git"}
    :merge-result {:base {...},
                  :checkout-conflicts nil, :conflicts nil, :failing-paths nil,
                  :merge-status "Already-up-to-date",
                  :merged-commits [...],
                  :new-head {...}},
    :rebase-result nil,
    :successful? true}

(git :push)
=> [{:advertised-refs [{:name "refs/heads/master",
                          :object-id "bf4d5affc89922117fba057457930e23e37439a3",
                          :peeled-object-id nil,
                          :storage "NETWORK", :peeled? true, :symbolic? false}],
       :messages "",
       :remote-updates [{:src-ref "refs/heads/master",
                         :expected-old-object-id nil,
                         :remote-name "refs/heads/master",
                         :status "UP_TO_DATE",
                         :expecting-old-object-id? false,
                         :new-object-id "bf4d5affc89922117fba057457930e23e37439a3",
                         :delete? false, :force-update? false,
                         :fast-forward? false, :message nil}],
       :tracking-ref-updates [{:local-name "refs/heads/master",
                               :new-object-id "bf4d5affc89922117fba057457930e23e37439a3",
                               :old-object-id "bf4d5affc89922117fba057457930e23e37439a3",
                               :remote-name "refs/heads/master",
                               :result "NO_CHANGE"}],
       :uri "git@github.com:zcaudate/gita.git"}]
```


#### alot more - please help!

I'm a git newbie compared to most and so I don't really know how to use a lot of commands properly or how they behave in the context of jgit. Some commands that can do with some TLC/use cases/pull requests are:
  
    :apply, :stash, :archive, :rebase, :notes, :cherry, 
    :blame, :revert, :tag, :clean, :ls, :submodule

So please have a play!

## License

Copyright © 2015 Chris Zheng

Distributed under the MIT License
