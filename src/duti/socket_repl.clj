(ns duti.socket-repl
  (:require
    [clojure.core.server :as server]
    [clojure.java.io :as io]))

(defn start-socket-repl
  ([]
   (start-socket-repl nil))
  ([opts]
   (let [port (:port opts)
         port (if (or (nil? port) (zero? port))
                (+ 1024 (rand-int 64512))
                port)]
     (println "Started Server Socket REPL on port" port)
     (when-some [port-file (:port-file opts ".repl-port")]
       (let [file (io/file port-file)]
         (spit file port)
         (.deleteOnExit file)))
     (server/start-server
       (merge
         {:name          "repl"
          :accept        'clojure.core.server/repl
          :server-daemon false}
         opts
         {:port port})))))
