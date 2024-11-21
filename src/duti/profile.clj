(ns duti.profile
  (:refer-clojure :exclude [time])
  (:require
   [clj-async-profiler.core :as profiler]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [criterium.core :as criterium])
  (:import
   [com.sun.management ThreadMXBean]
   [java.lang.management ManagementFactory]
   [java.text SimpleDateFormat]
   [java.util Date]))

(defn maybe-serve-ui []
  (when (nil? @@(requiring-resolve 'clj-async-profiler.ui/current-server))
    (profiler/serve-ui 9999)))

(profiler/set-default-profiling-options {:config {:sort-by :name}})

(defn profile [opts body]
  `(do
     (maybe-serve-ui)
     (profiler/start ~opts)
     (try
       ~@body
       (finally
         (println "Profiling finished" (str (profiler/stop)))))))

(defn profile-times [i opts body]
  (profile opts
    [`(dotimes [_# i]
       ~@body)]))

(defn profile-for [duration-ms opts body]
  (profile opts
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

(defn bench [opts body]
  (let [name (str/join " " body)
        name (if (< (count name) 100) name (str (subs name 0 100) "..."))]
    `(let [_#      (println (str *indent* "Benchmarking " (str/join " → " (conj *bench-stack* ~name))))
           bean#  ^ThreadMXBean (ManagementFactory/getThreadMXBean)
           bytes# (.getCurrentThreadAllocatedBytes bean#)
           res#    (criterium/benchmark* (fn [] ~@body) ~opts)
           mean#   (format-value (first (:mean res#)))
           stddev# (format-value (Math/sqrt (first (:variance res#))))
           calls#  (:execution-count res#)
           bytes#  (- (.getCurrentThreadAllocatedBytes bean#) bytes#)
           alloc#  (/ bytes# (+ (:execution-count res#) (:warmup-executions res#)))]
       (println (str *indent* "└╴Mean time: " mean# ", alloc: " (format "%.2f" (/ alloc# 1024.0)) " KB, stddev: " stddev# ", calls: " calls#)))))

(defn time [msg body]
  `(let [bean#  ^ThreadMXBean (ManagementFactory/getThreadMXBean)
         bytes# (.getCurrentThreadAllocatedBytes bean#)
         start# (System/nanoTime)
         res#   (binding [*indent* (str "  " *indent*)]
                  ~@body)
         bytes# (- (.getCurrentThreadAllocatedBytes bean#) bytes#)]
     (println (format "%s% 3d ms % 3.2f KB for %s"
                *indent* 
                (-> (System/nanoTime) (- start#) (/ 1000000) long)
                (-> bytes# (/ 1024.0))
                ~msg))
     res#))
