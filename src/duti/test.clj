(ns duti.test
  (:refer-clojure :exclude [test])
  (:require
    [clojure.test :as test]
    [lambdaisland.deep-diff2 :as ddiff]))

(def color-printer
  (ddiff/printer
    {:color-scheme
     {:delimiter nil ; [:bold :red]
      :tag       nil ; [:red]
      :nil       nil ; [:bold :black]
      :boolean   nil ; [:green]
      :number    nil ; [:cyan]
      :string    nil ; [:bold :magenta]
      :character nil ; [:bold :magenta]
      :keyword   nil ; [:bold :yellow]
      :symbol    nil
      :function-symbol nil ; [:bold :blue]
      :class-delimiter nil ; [:blue]
      :class-name      nil ; [:bold :blue]

      :lambdaisland.deep-diff2.printer-impl/insertion [:green]
      :lambdaisland.deep-diff2.printer-impl/deletion  [:red]
      :lambdaisland.deep-diff2.printer-impl/other     [:yellow]}}))

(defmethod test/report :fail [m]
  (test/with-test-out
    (test/inc-report-counter :fail)
    (println "\nFAIL in" (test/testing-vars-str m))
    (when (seq test/*testing-contexts*) (println (test/testing-contexts-str)))
    (when-some [message (:message m)]
      (println message))
    (let [expected-str (pr-str (:expected m))
          actual-str   (pr-str (:actual m))]
      (if (and
            (= '= (first (:expected m)))
            ; (or
            ;   (> (count expected-str) 50)
            ;   (> (count actual-str) 50))
            )
        (let [[_ [_ expected actual]] (:actual m)
              expected-str (pr-str expected)
              expr-str     (pr-str (nth (:expected m) 2))]
          (println "    expr:" expr-str)
          (println "expected:" expected-str)
          (print   "  actual: ")
          (-> (ddiff/diff expected actual)
            ; (ddiff/minimize)
            (ddiff/pretty-print color-printer)))
        (do
          (println "expected:" expected-str)
          (println "  actual:" actual-str))))))

(defmethod test/report :error [m]
  (test/with-test-out
    (test/inc-report-counter :error)
    (println "\nERROR in" (test/testing-vars-str m))
    (when (seq test/*testing-contexts*)
      (println (test/testing-contexts-str)))
    (let [{:keys [message actual expected]} m]
      (when message
        (println message))
      (if (= "Uncaught exception, not in assertion." message)
        (prn actual)
        (do
          (println "expected:" (pr-str expected))
          (if (instance? Throwable actual)
            (println "  actual:")
            (print   "  actual: "))
          (prn actual))))))

(defmethod test/report :summary [m]
  (test/with-test-out
    (println (str "\n" (:test m) " tests, "
               (+ (:pass m) (:fail m) (:error m)) " assertions, "
               (:fail m) " failures, "
               (:error m) " errors"))))

(defmethod test/report :begin-test-ns [m]
  (test/with-test-out
    (println "Testing" (ns-name (:ns m)))))

(defn test
  ([]
   (test #".*-test"))
  ([re]
   (let [vars (for [ns    (all-ns)
                    :when (re-matches re (name (ns-name ns)))
                    var   (vals (ns-interns (the-ns ns)))
                    :when (:test (meta var))
                    :when (:only (meta var))]
                var)]
     (if (empty? vars)
       (test/run-all-tests re)
       (binding [test/*report-counters* (ref test/*initial-report-counters*)]
         (test/test-vars vars)
         (test/do-report (assoc @test/*report-counters* :type :summary))
         @test/*report-counters*)))))

(defn test-throw
  ([]
   (test-throw #".*-test"))
  ([re]
   (let [{:keys [fail error] :as res} (test re)
         res (dissoc res :type)]
     (if (pos? (+ fail error))
       (throw (ex-info "Tests failed" res))
       res))))

(defn test-exit
  ([]
   (test-exit #".*-test"))
  ([re]
   (let [{:keys [fail error]} (test re)]
     (System/exit (+ fail error)))))
