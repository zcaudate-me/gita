(ns gita.macros)

(defn namespaced-method [sig namespace]
  (let [arglists  (:arglists sig)
        method    (:name sig)
        ns-method (symbol (str namespace "/" method))]
    `(~method ~@(map (fn [arglist]
                       `(~arglist ~(cons ns-method arglist)))
                     arglists))))

(defn namespaced-protocol [^clojure.lang.Var pvar pairs]
  (let [sigs  (-> pvar deref :sigs vals)
        pairs (partition 2 pairs)]
    `(extend-protocol ~(symbol (str (.ns pvar) "/" (.sym pvar)))
       ~@(mapcat (fn [[class namespace]]
                   (cons class (map #(namespaced-method % namespace) sigs)))
           pairs))))

(defmacro extend-namespaced [protocols & pairs]
  (let [curr-ns *ns*]
    `(do ~@(map (fn [protocol]
                  (namespaced-protocol
                   (ns-resolve curr-ns protocol)
                   pairs))
                protocols))))

(defn namespaced-defmethod [name args result namespace]
  `(defmethod ~name ~result ~args
     ~(cons (symbol (str namespace "/" name)) args)))
