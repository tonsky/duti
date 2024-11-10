(ns duti.profile
  (:refer-clojure :exclude [time])
  (:require
   [clj-async-profiler.core :as profiler]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [criterium.core :as criterium])
  (:import
   [java.text SimpleDateFormat]
   [java.util Date]))

(defn maybe-serve-ui []
  (when (nil? @@(requiring-resolve 'clj-async-profiler.ui/current-server))
    (profiler/serve-ui 9999)))

(defn profile [body]
  `(do
     (maybe-serve-ui)
     (profiler/start)
     (try
       ~@body
       (finally
         (println "Profiling finished" (str (profiler/stop)))))))

(defn profile-times [i body]
  (profile
    [`(dotimes [_# i]
       ~@body)]))

(defn profile-for [duration-ms body]
  (profile
    [`(let [deadline# (+ (System/currentTimeMillis) ~duration-ms)]
       (loop []
         (when (< (System/currentTimeMillis) deadline#)
           ~@(if (seq body) body [(list 'java.lang.Thread/sleep duration-ms)])
           (recur))))]))

(defn format-value [value]
  (let [[factor unit] (criterium/scale-time value)]
    (criterium/format-value value factor unit)))

(def ^:dynamic *indent*
  "")

(def ^:dynamic *bench-stack*
  [])

(defn benching [str body]
  `(binding [*bench-stack* (conj *bench-stack* ~str)]
     ~@body))

(defn bench [body]
  (let [name (str/join " " body)
        name (if (< (count name) 100) name (str (subs name 0 100) "..."))]
    `(let [_#      (println (str *indent* "Benchmarking " (str/join " → " (conj *bench-stack* ~name))))
           res#    (criterium/benchmark* (fn [] ~@body) {})
           mean#   (format-value (first (:mean res#)))
           stddev# (format-value (Math/sqrt (first (:variance res#))))
           calls#  (:execution-count res#)]
       (println (str *indent* "└╴Mean time: " mean# ", stddev: " stddev# ", calls: " calls#)))))

(defn time [msg & body]
  `(let [start# (System/nanoTime)
         res#   (binding [*indent* (str "  " *indent*)]
                  ~@body)]
     (println (str *indent* ~msg) "took" (-> (System/nanoTime) (- start#) (/ 1000000) long) "ms")
     res#))
