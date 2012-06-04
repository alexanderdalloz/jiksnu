(defproject net.kronkltd/jiksnu "0.1.0-SNAPSHOT"
  :description "distributed social network"
  :url "https://github.com/duck1123/jiksnu"
  :min-lein-version "2.0.0"
  :dependencies [
                 ;; [aleph "0.3.0-alpha2"]
                 [cheshire "4.0.0"]
                 [ciste "0.3.0-SNAPSHOT"]
                 [clj-factory "0.2.0"]
                 [clj-http "0.4.2"]
                 ;; [clj-stacktrace "0.2.4"]
                 ;; [clj-tigase "0.1.0-SNAPSHOT"]
                 ;; [com.draines/postal "1.7.1"]
                 [clojurewerkz/route-one "1.0.0-beta1"]
                 [clojurewerkz/urly "1.0.0"]
                 [clojurewerkz/quartzite "1.0.0-rc5"]
                 [clojurewerkz/elastisch "1.0.0-alpha4"]
                 [clojurewerkz/mailer "1.0.0-alpha3"]

                 [com.novemberain/monger "1.0.0-SNAPSHOT"]
                 [com.ocpsoft/ocpsoft-pretty-time "1.0.6"]
                 ;; [com.thoughtworks.xstream/xstream "1.4.2"]
                 [hiccup "1.0.0"]
                 [jayq "0.1.0-SNAPSHOT"]
                 ;; [karras "0.9.0"]
                 [lamina "0.5.0-alpha3"]
                 [net.kronkltd/clj-gravatar "0.1.0-SNAPSHOT"]
                 [net.kronkltd/plaza "0.1.0-SNAPSHOT"]
                 [oauthentic "0.0.6"]
                 [org.apache.abdera2/abdera2-client "2.0-SNAPSHOT"
                  :exclusions [org.eclipse.jetty:jetty-server]]
                 [org.apache.abdera2/abdera2-core "2.0-SNAPSHOT"]
                 [org.apache.abdera2/abdera2-ext "2.0-SNAPSHOT"]
                 ;; [org.clojars.sean_devlin/decorators "0.0.1-SNAPSHOT"]
                 [org.clojure/clojure "1.3.0"]
                 [org.clojure/core.cache "0.5.0"]
                 ;; [org.clojure/google-closure-library "0.0-1589"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.mindrot/jbcrypt "0.3m"]
                 [org.slf4j/slf4j-api "1.6.4"]
                 [org.slf4j/slf4j-log4j12 "1.6.4"]
                 [ring "1.1.0"]
                 [ring-basic-authentication "0.0.1"]
                 [slingshot "0.10.2"]
                 [xml-picker-seq "0.0.2"]
                 ]
  ;; :exclusions [
  ;;              com.rabbitmq/amqp-client
  ;;              org.apache.abdera/abdera-core
  ;;              org.clojure/contrib
  ;;              org.clojure/clojure-contrib
  ;;              org.clojure.contrib/prxml
  ;;              org.slf4j/slf4j-nop
  ;;              ring/ring-jetty-adapter
  ;;              ]
  ;; :hooks [leiningen.cljsbuild]
  :aot [
        jiksnu.xmpp.plugin
        jiksnu.xmpp.channels
        jiksnu.xmpp.user-repository
        ]
  :cljsbuild {:builds
              [{:source-path "src-cljs"
                :compiler
                {:output-to "resources/public/cljs/bootstrap.js"
                 :output-dir "resources/public/cljs"
                 :optimizations :whitespace
                 :pretty-print true}}]}
  :main ciste.runner
  :jvm-opts [
             "-server"
             "-XX:MaxPermSize=1024m"
             "-Dfile.encoding=UTF-8"
             ]
  :repositories {
                 "java-dot-net"          "http://download.java.net/maven/2"
                 "jiksnu-internal"       "http://build.jiksnu.com/repository/internal"
                 "jiksnu-snapshots"      "http://build.jiksnu.com/repository/snapshots"
                 "sonatype-oss-public"   "https://oss.sonatype.org/content/groups/public/"
                 "apache-repo-snapshots" "https://repository.apache.org/content/repositories/snapshots"
                 }
  :warn-on-reflection false

  :profiles {:dev
             {
              :dependencies
              [[midje "1.4.0"]
               [ring-mock "0.1.1"]
               [clj-webdriver "0.6.0-alpha7"]]}}

  :plugins [
               [lein-cljsbuild "0.1.5"]
               [lein-cucumber "1.0.0"]  
               [lein-midje "2.0.0-SNAPSHOT"]

            ]
  )
