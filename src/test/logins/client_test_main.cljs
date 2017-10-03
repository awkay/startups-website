(ns logins.client-test-main
  (:require logins.tests-to-run
            [fulcro-spec.selectors :as sel]
            [fulcro-spec.suite :as suite]))

(enable-console-print!)

(suite/def-test-suite client-tests {:ns-regex #"logins..*-spec"}
  {:default   #{::sel/none :focused}
   :available #{:focused}})

