(ns duti.all
  (:require
    [clojure.tools.namespace.repl :as ns]
    [duti.core :as core]
    [duti.error :as error]
    [duti.hashp :as hashp]
    [duti.ml :as ml]
    [duti.reload :as reload]
    [duti.socket-repl :as socket-repl]
    [duti.test :as test]))

(defn set-dirs [& dirs]
  (alter-var-root #'core/dirs (constantly (vec dirs)))
  (apply ns/set-refresh-dirs dirs))

(def ^{:arities '([] [opts])} reload
  reload/reload)

(def ^{:arities '([] [opts])} start-socket-repl
  socket-repl/start-socket-repl)

(def ^{:arities '([] [re])} test-throw
  test/test-throw)

(def ^{:arities '([] [re])} test-exit
  test/test-exit)

(alter-meta! (the-ns 'user) assoc 
  ::ns/load false)