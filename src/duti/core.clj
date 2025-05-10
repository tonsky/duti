(ns duti.core
  (:refer-clojure :exclude [time])
  (:require
   [clj-memory-meter.core :as memory-meter]
   [clojure.string :as str]
   [duti.profile :as profile]))

(defmacro profile
  "Runs body once and outputs report to /tmp/clj-async-profiler/reports"
  [& body]
  (let [[opts body] (if (map? (first body))
                      [(first body) (next body)]
                      [{} body])]
    (profile/profile opts body)))

(defmacro profile-times
  "Runs body `i` times and outputs report to /tmp/clj-async-profiler/reports"
  [i & body]
  (let [[opts body] (if (map? (first body))
                      [(first body) (next body)]
                      [{} body])]
    (profile/profile-times i opts body)))

(defmacro profile-for
  "Runs body for `duration-ms` ms and outputs report to /tmp/clj-async-profiler/reports"
  [duration-ms & body]
  (let [[opts body] (if (map? (first body))
                      [(first body) (next body)]
                      [{} body])]
    (profile/profile-for duration-ms opts body)))

(defmacro benching
  "Like `testing`, but for bench"
  [str & body]
  (profile/benching str body))
  
(defmacro long-bench
  "Runs body in a loop and prints median execution time"
  [& body]
  (let [[opts body] (if (map? (first body))
                      [(first body) (next body)]
                      [nil body])]
    (profile/bench (merge criterium.core/*default-benchmark-opts* opts) body)))

(defmacro bench
  "Runs body in a loop and prints median execution time"
  [& body]
  (let [[opts body] (if (map? (first body))
                      [(first body) (next body)]
                      [nil body])]
    (profile/bench (merge criterium.core/*default-quick-bench-opts* opts) body)))

(defn memory
  "Measures how much memory an object occupies"
  [obj]
  (memory-meter/measure obj))

(defn memory-layout
  "Prints object layout in memory"
  [obj]
  (memory-meter/measure obj :debug true))

(defmacro time
  "Like `time` but with a message and nesting"
  [& body]
  (if (string? (first body))
    (profile/time (first body) (next body))
    (let [msg (str/join " " (map pr-str body))
          msg (if (> (count msg) 60) (str (subs msg 0 57) "...") msg)]
      (profile/time msg body))))
