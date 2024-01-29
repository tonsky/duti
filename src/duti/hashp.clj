(ns duti.hashp
  (:require
    [clojure.walk :as walk])
  (:import
    [clojure.lang Compiler TaggedLiteral]))

(def p-lock
  (Object.))

(defn p-pos []
  (let [trace (->> (Thread/currentThread)
                (.getStackTrace)
                (seq))
        el    ^StackTraceElement (nth trace 4)]
    (str "[" (Compiler/demunge (.getClassName el)) " " (.getFileName el) ":" (.getLineNumber el) "]")))

(defn p-impl [position form res]
  (let [form (walk/postwalk
               (fn [form]
                 (if (and
                       (list? form)
                       (= 'duti.hashp/p-impl (first form)))
                   (TaggedLiteral/create 'p (nth form 3))
                   form))
               form)]
    (locking p-lock
      (println (str position " #p " form " => " (pr-str res))))
    res))

(defn p [form]
  `(p-impl (p-pos) '~form ~form))
