(ns gita.macros-test
  (:require [gita.macros :refer :all]
            [midje.sweet :refer :all]))

(defprotocol IData
  (to-data [obj] [obj more])
  (data-types [obj]))


(fact "generates method with namespace at the front"
 (namespaced-method
  (-> IData :sigs :data-types) 'test)
 => '(data-types ([obj] (test/data-types obj)))

 (namespaced-method
  (-> IData :sigs :to-data) 'test)
 => '(to-data ([obj] (test/to-data obj))
              ([obj more] (test/to-data obj more))))


(fact "generates protocol"
 (namespaced-protocol
  #'IData '[Enum enum String string])
 => '(clojure.core/extend-protocol gita.macros-test/IData
       Enum
       (data-types ([obj] (enum/data-types obj)))
       (to-data ([obj] (enum/to-data obj))
                ([obj more] (enum/to-data obj more)))
       String
       (data-types ([obj] (string/data-types obj)))
       (to-data ([obj] (string/to-data obj))
                ([obj more] (string/to-data obj more)))))


(fact "generates defmethods from namespace"
 (macroexpand-1
  '(extend-namespaced-multi
   (from-data [data type])

   Enum enum
   org.eclipse.jgit.lib.ObjectId object-id
   org.eclipse.jgit.lib.Repository repository) )
 => '[(clojure.core/defmethod from-data Enum
        [data type] (enum/from-data data type))
      (clojure.core/defmethod from-data org.eclipse.jgit.lib.ObjectId
        [data type] (object-id/from-data data type))
      (clojure.core/defmethod from-data org.eclipse.jgit.lib.Repository
        [data type] (repository/from-data data type))])
