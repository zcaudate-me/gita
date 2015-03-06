# gita

[![Build Status](https://travis-ci.org/zcaudate/gita.png?branch=master)](https://travis-ci.org/zcaudate/gita)

Git on Song

## Overview

`gita` is a clojure wrapper around the popular [jgit](https://eclipse.org/jgit/) project. The aim of the project is to:
  - make a git library that is simple and intuitive to use
  - generate the clojure interface through reflection
  - learn more about git in the process

## Installation

Add to project.clj dependencies:

```clojure
[im.chit/gita "0.1.0"]
```

## Usage

There is only one function: `git`. It's usage is very much like how the command-line version is used:

```
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
=> {:clean? true, :uncommitted-changes? false}

;; Now the default directory is set and we don't
;; need to set it next time we call:
(git :status)
=> {:clean? true, :uncommitted-changes? false}

;; We can also use `:cd` to set the default directory
(git :cd "/tmp/gita-example")
(git :status)
=> {:clean? true, :uncommitted-changes? false}

;; To check the current working directory, use `:pwd`
(git :pwd)
=> "/tmp/gita-example"

;; We add a file and then check for the repository status 
(spit "/tmp/gita-example/hello.txt" "hello there")
(git :status)
=> {:clean? false, :uncommitted-changes? false, :untracked #{"hello.txt"}}
```

#### getting help - :? and :help

```clojure
;; Lets take a look at a subcommand:
(git :init :?) ;; using `:help` does the same thing
;
;  Options are: {:git-dir java.lang.String, :directory java.lang.String, :bare boolean}  
;

;; Lets take a look at what other parameters we use with `:status`. To be perfectly honest, 
;; I'm not really sure what the options do, but here they are:
(git :status :?)
;
;  Options are:
;  {:working-tree-it   org.eclipse.jgit.treewalk.WorkingTreeIterator, 
;   :progress-monitor  org.eclipse.jgit.lib.ProgressMonitor, 
;   :ignore-submodules #{"NONE" "UNTRACKED" "DIRTY" "ALL"}, 
;   :path              [java.lang.String]}
```


#### - :add and :commit


## License

Copyright © 2015 Chris Zheng

Distributed under the MIT License