(ns duti.reload
  (:require
    [clojure.tools.namespace.repl :as ns]
    [clojure.tools.namespace.track :as track]))

(defn reload
  ([]
   (reload nil))
  ([opts]
   (set! *warn-on-reflection* true)
   (let [opts    (merge {:only-active? true} opts)
         tracker (ns/scan opts)
         cnt     (count (::track/load tracker))
         res     (apply ns/refresh-scanned (mapcat vec opts))]
     (when (instance? Throwable res)
       (throw res))
     (str "Reloaded " cnt " namespace" (when (> cnt 1) "s")))))
