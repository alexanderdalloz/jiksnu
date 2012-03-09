(defproject net.kronkltd/jiksnu "0.1.0-SNAPSHOT"
  :description "distributed social network"
  :url "http://github.com/duck1123/jiksnu"
  :dependencies [
                 [aleph "0.2.1-alpha2-SNAPSHOT"]
                 [ciste "0.3.0-SNAPSHOT"]
                 [clj-factory "0.2.0"]
                 [clj-http "0.3.2"]
                 [clj-tigase "0.1.0-SNAPSHOT"]
                 [clj-time "0.3.5"]
                 [com.ocpsoft/ocpsoft-pretty-time "1.0.6"]
                 [crate "0.1.0-SNAPSHOT"]
                 [enlive "1.0.0"]
                 [hiccup "0.3.8"]
                 [jayq "0.1.0-SNAPSHOT"]
                 [karras "0.9.0"]
                 [net.kronkltd/clj-gravatar "0.1.0-SNAPSHOT"]
                 [net.kronkltd/plaza "0.0.6-SNAPSHOT"]
                 [org.apache.abdera2/abdera2-client "2.0-SNAPSHOT"
                  :exclusions [org.eclipse.jetty:jetty-server]]
                 [org.apache.abdera2/abdera2-core "2.0-SNAPSHOT"]
                 [org.apache.abdera2/abdera2-ext "2.0-SNAPSHOT"]
                 [org.clojure/clojure "1.3.0"]
                 [org.clojure/core.cache "0.5.0"]
                 [org.clojure/google-closure-library "0.0-1589"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.mindrot/jbcrypt "0.3m"]
                 [org.slf4j/slf4j-api "1.6.4"]
                 [org.slf4j/slf4j-log4j12 "1.6.4"]
                 [ring "1.0.2"]
                 [ring-basic-authentication "0.0.1"]
                 [solrclj "0.1.2"]
                 [xml-picker-seq "0.0.2"]
                 ]
  :dev-dependencies [
                     [midje "1.3.2-SNAPSHOT" :exclusions [org.clojure/clojure]]
                     [ring-mock "0.1.1"]
                     [lein-cljsbuild "0.1.2"]
                     [lein-midje "1.0.8"]
                     [clj-webdriver "0.6.0-alpha4"]
                     [fluentsoftware/lein-cucumber "1.0.0-SNAPSHOT"]
                     ]
  :java-source-path "src"
  ;; :checksum-deps false
  :exclusions [
               com.rabbitmq/amqp-client
               org.apache.abdera/abdera-core
               org.clojure/contrib
               org.clojure/clojure-contrib
               org.slf4j/slf4j-nop
               ring/ring-jetty-adapter
               ]
  :hooks [leiningen.cljsbuild]
  :aot [
        jiksnu.xmpp.plugin
        jiksnu.xmpp.channels
        jiksnu.xmpp.user-repository
        ]
  :cljsbuild {:builds [{:source-path "src-cljs"
                        :compiler {
                                   :output-to "resources/public/cljs/bootstrap.js"
                                   :output-dir "resources/public/cljs"
                                   :optimizations :whitespace
                                   :pretty-print true
                                   }}]}
  :main ciste.runner
  :warn-on-reflection false
  :jvm-opts [
             "-server"
             "-XX:MaxPermSize=1024m"
             "-Dfile.encoding=UTF-8"
             ]
  ;; :repositories {
  ;;                "java-dot-net" "http://download.java.net/maven/2"
  ;;                "jiksnu-internal" "http://build.jiksnu.com/repository/internal"
  ;;                "jiksnu-snapshots" "http://build.jiksnu.com/repository/snapshots"
  ;;                "sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  )
