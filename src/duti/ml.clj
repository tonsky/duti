(ns duti.ml
  (:require
    [clojure.string :as str]))

(defn reindent ^String [s indent]
  (let [lines    (str/split-lines s)
        butfirst (->> lines
                   next
                   (remove str/blank?))]
    (if (seq butfirst)
      (let [prefix (->> butfirst
                     (map #(count (second (re-matches #"( *).*" %))))
                     (reduce min))]
        (str/join "\n"
          (cons
            (str indent (first lines))
            (map #(str indent (subs % prefix)) (next lines)))))
      s)))

(defn ml [s]
  (assert (string? s))
  (reindent s ""))
