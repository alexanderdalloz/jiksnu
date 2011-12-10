(defproject net.kronkltd/jiksnu "0.1.0-SNAPSHOT"
  :description "distributed social network"
  :url "http://github.com/duck1123/jiksnu"
  :repositories {"any23-repository-external" "http://any23.googlecode.com/svn/repo-ext"
                 "java-dot-net" "http://download.java.net/maven/2"
                 "any23-repository" "http://any23.googlecode.com/svn/repo"
                 "sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :dependencies [
                 [clj-factory "0.2.0-SNAPSHOT"]
                 [clj-tigase "0.1.0-SNAPSHOT"]
                 [clj-time "0.3.3"]
                 [clj-webdriver "0.4.0"]
                 [closure-templates-clj "0.1.2-SNAPSHOT"]
                 [ciste "0.2.0-SNAPSHOT"]
                 [com.ocpsoft/ocpsoft-pretty-time "1.0.6"]
                 [compojure "0.6.5"]
                 [dom4j "1.6.1"]
                 [hiccup "0.3.7"]
                 [net.kronkltd/aleph "0.2.1-SNAPSHOT"]
                 [net.kronkltd/clj-gravatar "0.0.1"]
                 [joda-time "2.0"]
                 [lamina "0.4.1-SNAPSHOT"]
                 [net.kronkltd/karras "0.8.1-SNAPSHOT"]
                 [net.kronkltd/plaza "0.0.6-SNAPSHOT"]
                 [org.apache.abdera2/abdera2-client "2.0-SNAPSHOT"]
                 [org.apache.abdera2/abdera2-core "2.0-SNAPSHOT"]
                 [org.apache.abdera2/abdera2-ext "2.0-SNAPSHOT"]
                 [org.clojure/clojure "1.3.0"]
                 [org.clojure/tools.logging "0.1.2"]
                 [org.slf4j/slf4j-api "1.6.1"]
                 [org.slf4j/slf4j-simple "1.6.1"]
                 [pinot "0.1.1-SNAPSHOT"]
                 [potemkin "0.1.0"]
                 [ring "1.0.0-RC5"]
                 [swank-clojure "1.3.3"]
                 [xml-picker-seq "0.0.2"]
                 ]
  :dev-dependencies [
                     [org.clojars.rferraz/lein-cuke "1.1.1"]
                     [midje "1.3-alpha6" :exclusions [org.clojure/clojure]]
                     [ring-mock "0.1.1"]
                     [lein-marginalia "0.6.1"]
                     ]
  :exclusions [
               com.rabbitmq/amqp-client
               org.apache.abdera/abdera-core
               org.clojure/contrib
               org.clojure/clojure-contrib
               ring/ring-jetty-adapter
               ]
  :aot [
        jiksnu.xmpp.plugin
        jiksnu.xmpp.channels
        jiksnu.xmpp.user-repository
        ]
  :main jiksnu.core
  :warn-on-reflection false
  :jvm-opts ["-server"
             "-XX:MaxPermSize=1024m"])
