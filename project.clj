(defproject vpurge "0.1.0-SNAPSHOT"
  :description "A simple tool to distribute purges to multiple varnish nodes through rabbitmq"
  :url "https://github.com/kautsig/vpurge"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"],
                 [org.clojure/tools.logging "0.2.4"]
                 [org.clojure/tools.cli "0.3.3"]
                 [com.novemberain/langohr "3.4.2"]
                 [org.slf4j/slf4j-log4j12 "1.7.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jmdk/jmxtools
                                                    com.sun.jmx/jmxri]]]
  :main ^:skip-aot vpurge.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
