(ns vpurge.producer
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [clojure.string :as str]
            [langohr.core :as rmq]
            [langohr.channel :as lch]
            [langohr.queue :as lq]
            [langohr.consumers :as lc]
            [langohr.exchange :as le]
            [langohr.basic :as lb]))

(import java.util.concurrent.Executors)

(def ^:const varnish-exchange "varnish-exchange")

(defn construct-message
  []
  (str "/" (rand-int 1000)))

(defn publish-message
  "Publishes an example message to be processed by the consumer"
  [ch routing-key]
  (lb/publish ch varnish-exchange routing-key (construct-message) {:content-type "text/plain" :type "greetings.hi"}))

(defn -main
  [& args]

  (let [conn (rmq/connect {:username "admin"
                           :password "nimda"
                           :executor (Executors/newFixedThreadPool 16)})
        ch (lch/open conn)
        routing-key "varnish-routing-key"]
    
    (log/info (format "Connected. Channel id: %d" (.getChannelNumber ch)))
    (le/declare ch varnish-exchange "topic" {:durable false :auto-delete true})
    
    (log/info "Publishing...")

    (doseq [x (range 100)]
      (publish-message ch routing-key))
    
    (rmq/close ch)
    (rmq/close conn)
    (System/exit 0)))
