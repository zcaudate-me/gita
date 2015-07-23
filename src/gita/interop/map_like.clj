(ns gita.interop.map-like
  (:require [hara.object :as object]))

(object/extend-maplike
 org.eclipse.jgit.dircache.DirCacheEntry      {:tag "e" :exclude [:raw-mode :raw-path]}
 org.eclipse.jgit.api.ApplyResult             {:tag "apply"}
 org.eclipse.jgit.api.CheckoutResult          {:tag "checkout"}
 org.eclipse.jgit.api.CherryPickResult        {:tag "cherrypick"}
 org.eclipse.jgit.api.MergeResult             {:tag "merge"}
 org.eclipse.jgit.api.RebaseResult            {:tag "rebase"}
 org.eclipse.jgit.api.PullResult              {:tag "pull"}
 org.eclipse.jgit.lib.ReflogEntry             {:tag "entry"}
 org.eclipse.jgit.lib.Ref                     {:tag "ref" :exclude [:leaf :target]}
 org.eclipse.jgit.diff.DiffEntry              {:tag "entry"}
 org.eclipse.jgit.transport.OperationResult   {:tag "result"}
 org.eclipse.jgit.revwalk.RevCommit           {:tag "commit"
                                               :select [:commit-time :name :author-ident :full-message]}
 org.eclipse.jgit.lib.PersonIdent             {:tag "person" :exclude [:time-zone]}
 org.eclipse.jgit.transport.RemoteRefUpdate   {:tag "remote" :exclude [:tracking-ref-update]}
 org.eclipse.jgit.transport.TrackingRefUpdate {:tag "track"})
