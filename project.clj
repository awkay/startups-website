(defproject startupsite "0.1.0-SNAPSHOT"
  :description "Startup Website"
  :license {:name "Private"}
  :min-lein-version "2.7.0"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]

                 ; ui
                 [cljsjs/semantic-ui-react "0.73.0-0"]
                 [fulcrologic/semantic-ui-react-wrappers "1.0.0"]
                 [fulcrologic/fulcro "2.1.2"]
                 [cljsjs/react-dom-server "15.6.2-1"]
                 [fulcrologic/fulcro-css "2.0.0"]

                 ; server
                 [com.draines/postal "2.0.2"]
                 [fulcrologic/fulcro-sql "0.3.0"]
                 [org.postgresql/postgresql "42.1.4"]

                 ; routing
                 [bidi "2.1.2"]
                 [kibu/pushy "0.3.8"]

                 ; pinned
                 [cljsjs/react "15.6.2-1"]
                 [cljsjs/react-dom "15.6.2-1"]
                 [commons-codec "1.11"]

                 ; test
                 [fulcrologic/fulcro-spec "2.0.0-beta3" :scope "test"]]

  :uberjar-name "startupsite.jar"

  :source-paths ["src/main" "src/server"]
  :test-paths ["src/test"]
  :clean-targets ^{:protect false} ["target" "resources/public/js" "resources/private"]

  :cljsbuild {:builds [{:id           "production"
                        :source-paths ["src/main"]
                        :jar          true
                        :compiler     {:asset-path     "js/prod"
                                       :parallel-build true
                                       :optimizations  :advanced
                                       :main           "startupsite.client-main"
                                       :externs        ["externs.js"]
                                       :output-dir     "resources/public/js/prod"
                                       :output-to      "resources/public/js/startupsite.min.js"
                                       :source-map     "resources/public/js/startupsite.min.js.map"}}
                       {:id           "ssr"
                        :source-paths ["src/main"]
                        :jar          true
                        :compiler     {:asset-path     "js/prod"
                                       :parallel-build true
                                       :optimizations  :advanced
                                       :main           "startupsite.nashorn-rendering"
                                       :externs        ["externs.js"]
                                       :output-dir     "resources/public/js/ssr"
                                       :output-to      "resources/public/js/startupsite.ssr.js"
                                       :source-map     "resources/public/js/startupsite.ssr.js.map"}}]}

  :profiles {:uberjar    {:main           startupsite.server-main
                          :aot            :all
                          :jar-exclusions [#"public/js/prod" #"com/google.*js$"]
                          :prep-tasks     ["clean" ["clean"]
                                           "compile" ["with-profile" "production" "cljsbuild" "once" "production"]
                                           "compile" ["with-profile" "production" "cljsbuild" "once" "ssr"]]}
             :production {}
             :dev        {:source-paths ["src/dev" "src/main" "src/test" "src/cards"]

                          :jvm-opts     ["-XX:-OmitStackTraceInFastThrow" "-client" "-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1"
                                         "-Xmx1g" "-XX:+UseConcMarkSweepGC" "-XX:+CMSClassUnloadingEnabled" "-Xverify:none"]

                          :doo          {:build "automated-tests"
                                         :paths {:karma "node_modules/karma/bin/karma"}}

                          :figwheel     {:css-dirs        ["resources/public/css"]
                                         :validate-config false}

                          :test-refresh {:report       fulcro-spec.reporters.terminal/fulcro-report
                                         :with-repl    true
                                         :changes-only true}

                          :cljsbuild    {:builds
                                         [{:id           "dev"
                                           :figwheel     {:on-jsload "cljs.user/mount"}
                                           :source-paths ["src/dev" "src/main"]
                                           :compiler     {:asset-path           "js/dev"
                                                          :main                 cljs.user
                                                          :optimizations        :none
                                                          :parallel-build       true
                                                          :output-dir           "resources/public/js/dev"
                                                          :output-to            "resources/public/js/startupsite.js"
                                                          :preloads             [devtools.preload fulcro.inspect.preload]
                                                          :source-map-timestamp true}}
                                          {:id           "i18n" ;for gettext string extraction
                                           :source-paths ["src/main"]
                                           :compiler     {:asset-path    "i18n"
                                                          :main          startupsite.client-main
                                                          :optimizations :whitespace
                                                          :output-dir    "i18n/tmp"
                                                          :output-to     "i18n/i18n.js"}}
                                          {:id           "test"
                                           :source-paths ["src/test" "src/main"]
                                           :figwheel     {:on-jsload "startupsite.client-test-main/client-tests"}
                                           :compiler     {:asset-path    "js/test"
                                                          :main          startupsite.client-test-main
                                                          :optimizations :none
                                                          :output-dir    "resources/public/js/test"
                                                          :output-to     "resources/public/js/test/test.js"
                                                          :preloads      [devtools.preload]}}
                                          {:id           "automated-tests"
                                           :source-paths ["src/test" "src/main"]
                                           :compiler     {:asset-path    "js/ci"
                                                          :main          startupsite.CI-runner
                                                          :optimizations :none
                                                          :output-dir    "resources/private/js/ci"
                                                          :output-to     "resources/private/js/unit-tests.js"}}
                                          {:id           "cards"
                                           :figwheel     {:devcards true}
                                           :source-paths ["src/main" "src/cards"]
                                           :compiler     {:asset-path           "js/cards"
                                                          :main                 startupsite.cards
                                                          :optimizations        :none
                                                          :output-dir           "resources/public/js/cards"
                                                          :output-to            "resources/public/js/cards.js"
                                                          :preloads             [devtools.preload fulcro.inspect.preload]
                                                          :source-map-timestamp true}}]}

                          :plugins      [[lein-cljsbuild "1.1.7"]
                                         [lein-doo "0.1.8"]
                                         [com.jakemccrary/lein-test-refresh "0.21.1"]]

                          :dependencies [[binaryage/devtools "0.9.8"]
                                         [hawk "0.2.11"]
                                         [org.clojure/tools.nrepl "0.2.13"]
                                         [fulcrologic/fulcro-inspect "2.0.0-alpha4"]
                                         [org.clojure/tools.namespace "0.3.0-alpha4"]
                                         [lein-doo "0.1.7" :scope "test"]
                                         [figwheel-sidecar "0.5.14"]
                                         [devcards "0.2.4" :exclusions [cljsjs/react]]]
                          :repl-options {:init-ns user}}})
