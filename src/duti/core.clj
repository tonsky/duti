(ns duti.core
  (:refer-clojure :exclude [test time])
  (:require
   [clj-memory-meter.core :as memory-meter]
   [clj-reload.core :as reload]
   [clojure.string :as str]
   [duti.common :as common]
   [duti.error :as error]
   [duti.hashp :as hashp]
   [duti.profile :as profile]
   [duti.socket-repl :as socket-repl]
   [duti.test :as test]))

(defn set-dirs [& dirs]
  (.bindRoot #'common/dirs (vec dirs))
  (reload/init
    {:dirs (vec dirs)
     :no-reload '#{user}}))

(defn reload
  ([]
   (reload nil))
  ([opts]
   (set! *warn-on-reflection* true)
   (let [res (reload/reload opts)
         cnt (count (:loaded res))]
     (str "Reloaded " cnt " namespace" (when (not= 1 cnt) "s")))))

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

(def ^{:arities '([] [opts])} start-socket-repl
  socket-repl/start-socket-repl)

(defn -main [& args]
  (.bindRoot #'*command-line-args* args)
  (let [{port "--port"} args]
    (socket-repl/start-socket-repl {:port (some-> port parse-long)})))

(def ^{:arities '([] [re])} test
  test/test)

(def ^{:arities '([] [re])} test-throw
  test/test-throw)

(def ^{:arities '([] [re])} test-exit
  test/test-exit)
