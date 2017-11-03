(ns startupsite.client-test-main
  (:require startupsite.tests-to-run
            [fulcro-spec.selectors :as sel]
            [fulcro-spec.suite :as suite]))

(enable-console-print!)

(suite/def-test-suite client-tests {:ns-regex #"startupsite..*-spec"}
  {:default   #{::sel/none :focused}
   :available #{:focused}})

