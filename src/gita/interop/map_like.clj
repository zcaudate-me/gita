(ns gita.interop.map-like
  (:require [gita.interop.common :as common]
            [gita.interop.util :as util]))

(defmacro extend-maplike-class [cls opts]
  `(vector
     (defmethod common/-meta-object ~cls
       [type#]
       {:class     type#
        :types     #{java.util.Map}
        :to-data   common/-to-data})

     (extend-protocol common/IData
       ~cls
       (-to-data [entry#]
         (-> (util/object-methods entry#)
             (dissoc ~@(:exclude opts) :class)
             ~@(if (:select opts)
                 `((select-keys ~(or (:select opts) []))))
             (util/object-apply entry# common/to-data))))

     (defmethod print-method ~cls
       [v# ^java.io.Writer w#]
       (.write w# (str "#" ~(:slug opts) "::" (common/-to-data v#))))))

(defmacro extend-maplike [& {:as classes}]
  `(vector ~@(map (fn [[cls opts]]
                    `(extend-maplike-class ~cls ~opts))
                  classes)))

(extend-maplike
 org.eclipse.jgit.dircache.DirCacheEntry     {:slug "e" :exclude [:raw-mode :raw-path]}
 org.eclipse.jgit.api.ApplyResult            {:slug "apply"}
 org.eclipse.jgit.api.CheckoutResult         {:slug "checkout"}
 org.eclipse.jgit.api.CherryPickResult       {:slug "cherrypick"}
 org.eclipse.jgit.api.MergeResult            {:slug "merge"}
 org.eclipse.jgit.api.RebaseResult           {:slug "rebase"}
 org.eclipse.jgit.api.PullResult             {:slug "pull"}
 org.eclipse.jgit.lib.ReflogEntry            {:slug "entry"}
 org.eclipse.jgit.lib.Ref                    {:slug "ref" :exclude [:leaf :target]}
 org.eclipse.jgit.diff.DiffEntry             {:slug "entry"}
 org.eclipse.jgit.transport.OperationResult  {:slug "result"}
 org.eclipse.jgit.revwalk.RevCommit          {:slug "commit"
                                              :select [:commit-time :name :author-ident :full-message]}
 org.eclipse.jgit.lib.PersonIdent            {:slug "person" :exclude [:time-zone]})
