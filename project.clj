(defproject net.kronkltd/jiksnu "0.2.0-SNAPSHOT"
  :description "distributed social network"
  :url "https://github.com/duck1123/jiksnu"
  :author "Daniel E. Renfer <duck@kronkltd.net>"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src" "src-cljs"]
  :resource-paths ["resources" "target/resources"]
  :dependencies [[cider/cider-nrepl "0.10.2"]
                 [ciste "0.6.0-SNAPSHOT"
                  :exclusions [ring/ring-core
                               org.clojure/tools.reader
                               org.clojure/clojurescript
                               ;; xom
                               ]]
                 [ciste/ciste-incubator "0.1.0-SNAPSHOT"
                  :exclusions [ciste ciste/ciste-core]]
                 [clj-factory "0.2.2-SNAPSHOT"]
                 [clj-time "0.11.0"]
                 [clj-http "2.0.1"]
                 [clojurewerkz/mailer "1.3.0"]
                 [compojure "1.4.0"]
                 [clojurewerkz/support "1.1.0"]
                 [com.cemerick/friend "0.2.1"]
                 [com.getsentry.raven/raven "7.0.0"]
                 [com.flybe/socket-rocket "0.1.9"]
                 ;; [com.fzakaria/slf4j-timbre "0.3.0"]
                 [com.novemberain/monger "3.0.2"]
                 [com.novemberain/validateur "2.5.0"]
                 [com.taoensso/timbre "4.3.1"]
                 [crypto-random "1.2.0"]
                 [hiccup "1.0.5"]
                 [im.chit/gyr "0.3.1"
                  :exclusions [im.chit/purnam]]
                 [io.kamon/kamon-core_2.11 "0.4.0"]
                 [io.kamon/kamon-statsd_2.11 "0.4.0"]
                 [io.kamon/kamon-system-metrics_2.11 "0.4.0"]
                 ;; [io.kamon/kamon-log-reporter_2.11 "0.4.0"]
                 [net.kronkltd/purnam "0.5.3-SNAPSHOT"]
                 [javax.servlet/javax.servlet-api "3.1.0"]
                 [liberator "0.14.0"]
                 [manifold "0.1.2"]
                 [mvxcvi/puget "1.0.0"]
                 [net.kronkltd/clj-gravatar "0.1.0-SNAPSHOT"]
                 [net.kronkltd/octohipster "0.3.0-SNAPSHOT"
                  :exclusions [inflections]]
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/tools.reader "0.10.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.jsoup/jsoup "1.8.3"]
                 [org.slf4j/slf4j-api "1.7.16"]
                 [org.slf4j/slf4j-log4j12 "1.7.16"]
                 [ring/ring-core "1.4.0"]
                 [ring-basic-authentication "1.0.5"]
                 [slingshot "0.12.2"]]
  :plugins [[cider/cider-nrepl "0.10.2"]
            [codox "0.8.13"]
            [lein-annotations "0.1.0"]
            [lein-bikeshed "0.2.0"]
            [lein-checkall "0.1.1"]
            [lein-checkouts "1.1.0"]
            [lein-cljsbuild "1.1.2"]
            [lein-cloverage "1.0.2"]
            [lein-environ "1.0.0"]
            [lein-less "1.7.5"]
            [lein-midje "3.1.3"]
            [lein-shell "0.4.0"]
            [org.clojars.strongh/lein-init-script "1.3.1"]]
  :bower {:package-file "bower.json", :config-file ".bowerrc"}
  :hiera {:ignore-ns #{"jiksnu.ops"
                       "jiksnu.model"
                       "jiksnu.factory"
                       "jiksnu.namespace"
                       "jiksnu.channels"
                       "jiksnu.session"
                       "jiksnu.util"
                       "jiksnu.mock"
                       "jiksnu.registry"
                       "jiksnu.db"
                       }}
  :aliases {"guard"             ["shell" "bundle" "exec" "guard"]
            "karma"             ["shell" "./node_modules/.bin/karma" "start"]
            "protractor"        ["shell" "./node_modules/.bin/protractor" "protractor.config.js"]
            "webdriver-start"  ["shell" "./node_modules/.bin/webdriver-manager" "start"]
            "webdriver-update" ["shell" "./node_modules/.bin/webdriver-manager" "update"]
            "wscat"             ["shell" "./node_modules/.bin/wscat" "-c" "ws://localhost:8080/"]}
  :auto-clean false
  :jvm-opts ["-server"
             "-Dfile.encoding=UTF-8"
             "-Djava.library.path=native"
             ;; "-Dcom.sun.management.jmxremote"
             ;; "-Dcom.sun.management.jmxremote.ssl=false"
             ;; "-Dcom.sun.management.jmxremote.authenticate=false"
             ;; "-Dcom.sun.management.jmxremote.port=43210"
             ]
  :warn-on-reflection false
  :repl-options {:init-ns ciste.runner
                 :host    "0.0.0.0"
                 :port    7888}
  :appenders {:jl (make-tools-logging-appender {})}
  :main ciste.runner
  :aot [ciste.runner]
  :cljsbuild {:builds
              {:main {:source-paths ["src-cljs"]
                      :notify-command ["notify-send"]
                      :compiler {:output-to "target/resources/public/cljs/jiksnu.js"
                                 :output-dir "target/resources/public/cljs"
                                 :source-map "target/resources/public/cljs/jiksnu.js.map"
                                 ;; :main "jiksnu.app"
                                 :optimizations :simple
                                 :asset-path "cljs"
                                 ;; :verbose true
                                 :pretty-print true}}}}
  :profiles {:dev        [:dev-core :user-dev]
             :dev-core   {:dependencies
                          [[midje "1.8.3" :exclusions [org.clojure/clojure]]
                           [clj-factory "0.2.2-SNAPSHOT"]
                           [org.clojure/tools.nrepl "0.2.12"]
                           [ring-mock "0.1.5"]
                           [slamhound "1.5.5"]
                           [com.palletops/log-config "0.1.4"]]}
             :reporting {:dependencies [[helpshift/hydrox "0.1.15"]]}
             :e2e {:dependencies [[clj-webdriver "0.7.2" :exclusions [xalan]]
                                  [org.seleniumhq.selenium/selenium-java "2.52.0"]]
                   :cljsbuild {:builds
                               {:protractor
                                {:source-paths ["specs"]
                                 :notify-command ["notify-send"]
                                 :compiler {
                                            :output-to "target/protractor-tests.js"
                                            ;; :output-dir "target/specs/"
                                            :optimizations :simple
                                            :target :nodejs
                                            :pretty-print true}}}}}
             :production {:aot   :all
                          :hooks [leiningen.cljsbuild leiningen.less]
                          :cljsbuild {:builds
                                      {:advanced
                                       {:source-paths ["src-cljs"]
                                        :notify-command ["notify-send"]
                                        :compiler {:output-to "target/resources/public/cljs/jiksnu.min.js"
                                                   :optimizations :advanced
                                                   :pretty-print false}}}}}
             :test       {:resource-paths ["target/resources" "resources" "test-resources"]
                          :cljsbuild {:builds
                                      {:karma
                                       {:source-paths ["src-cljs" "test-cljs"]
                                        :notify-command ["notify-send"]
                                        :compiler {:output-to "target/karma-test.js"
                                                   :output-dir "target/karma"
                                                   :optimizations :simple
                                                   ;; Fix for $q's use of 'finally'
                                                   :language-in :ecmascript5
                                                   :pretty-print true}}}}}}
  :less {:source-paths ["less"]
         :target-path  "target/resources/public/css"}
  :env {:ciste-logger "jiksnu.logger"}
  :filespecs [{:type :path :path "ciste.clj"}]
  :lis-opts {:name       "jiksnu"
             :properties {:ciste.properties "/vagrant/config/default.properties"}
             :jvm-opts   ["-server"]}
  :repositories [["snapshots" {:url "http://artifactory.jiksnu.com/artifactory/libs-snapshot-local/"
                               :username [:gpg :env/artifactory_username]
                               :password [:gpg :env/artifactory_password]}]
                 ["releases" {:url "http://artifactory.jiksnu.com/artifactory/libs-releases-local/"
                              :username [:gpg :env/artifactory_username]
                              :password [:gpg :env/artifactory_password]}]])
