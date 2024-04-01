(ns duti.core
  (:refer-clojure :exclude [test])
  (:require
    [clj-reload.core :as reload]
    [duti.common :as common]
    [duti.error :as error]
    [duti.hashp :as hashp]
    [duti.socket-repl :as socket-repl]
    [duti.test :as test]))

(defn set-dirs [& dirs]
  (alter-var-root #'common/dirs (constantly (vec dirs)))
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

(def ^{:arities '([] [opts])} start-socket-repl
  socket-repl/start-socket-repl)

(def ^{:arities '([] [re])} test
  test/test)

(def ^{:arities '([] [re])} test-throw
  test/test-throw)

(def ^{:arities '([] [re])} test-exit
  test/test-exit)
