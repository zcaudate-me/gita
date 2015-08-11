(ns gita.api.commands-test
  (:require [gita.api.commands :refer :all]
            [hara.object :as object]
            [hara.object.access :as access]
            [gita.interop :as interop]
            [midje.sweet :refer :all]
            [hara.reflect :as reflect]
            [clojure.java.io :as io])
  (:import org.eclipse.jgit.api.Git))

(fact "list all commands that work with git"
 (-> (git-all-commands) keys sort)
 => '(:add :apply :archive :blame :branch :checkout :cherry :clean :clone :commit :describe :diff :fetch :gc :init :log :ls :merge :name :notes :pull :push :rebase :reflog :reset :revert :rm :stash :status :submodule :tag)

 (->> (git-all-commands)
      (filter (fn [[k v]] (not (empty? v))))
      (into {}))
 => {:cherry #{:pick},
     :name #{:rev},
     :submodule #{:init :update :sync :status :add},
     :ls #{:remote},
     :clone #{:repository},
     :notes #{:remove :list :add :show},
     :stash #{:create :drop :list :apply},
     :tag #{:delete :list},
     :branch #{:create :delete :rename :list}})

(fact "list options for a particular command"
  (->> (command-options (Git/init))
       (reduce-kv (fn [m k v]
                    (assoc m k (command-input v)))
                  {}))
  => {:git-dir String, :directory String, :bare Boolean/TYPE})

(fact "initialize inputs for a particular command "
 (->> (Git/init)
      (reflect/delegate)
      (into {}))
 => {:directory nil, :bare false, :gitDir nil}

 (-> (Git/init)
     (command-initialize-inputs [:bare true
                                 :git-dir  (io/file ".git")])
     (reflect/delegate)
     (->> (into {})))
 =>  {:directory nil, :bare true, :gitDir (io/file ".git")})

(fact "check if application with coercion works"
 (let [init-command (Git/init)
       set-dir  (reflect/query-class init-command [:# "setGitDir"])]
   (->> (access/apply-with-coercion set-dir [init-command ".git"])
        (reflect/delegate)
        (into {})))
 => {:directory nil, :bare false, :gitDir (io/file ".git")})
