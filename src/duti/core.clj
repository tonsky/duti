(ns duti.core
  (:refer-clojure :exclude [test])
  (:require
    [duti.common :as common]
    [duti.error :as error]
    [duti.hashp :as hashp]
    [duti.socket-repl :as socket-repl]
    [duti.test :as test]))

(defn set-dirs [& dirs]
  (alter-var-root #'common/dirs (constantly (vec dirs))))

(def ^{:arities '([] [opts])} start-socket-repl
  socket-repl/start-socket-repl)

(def ^{:arities '([] [re])} test
  test/test)

(def ^{:arities '([] [re])} test-throw
  test/test-throw)

(def ^{:arities '([] [re])} test-exit
  test/test-exit)
