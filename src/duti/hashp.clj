(ns duti.hashp
  (:require
    [clojure.walk :as walk])
  (:import
    [clojure.lang Compiler TaggedLiteral]))

(def p-lock
  (Object.))

(defn p-impl [form res]
  (let [form (walk/postwalk
               (fn [form]
                 (if (and
                       (list? form)
                       (= 'duti.hashp/p-impl (first form)))
                   (TaggedLiteral/create 'p (nth form 2))
                   form))
               form)]
    (locking p-lock
      (println (str " #p " form " => " (pr-str res))))
    res))

(defn p [form]
  `(p-impl '~form ~form))
