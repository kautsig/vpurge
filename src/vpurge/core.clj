(ns vpurge.core
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [langohr.core :as rmq]
            [langohr.channel :as lch]
            [langohr.queue :as lq]
            [langohr.consumers :as lc]
            [langohr.exchange :as le]
            [langohr.basic :as lb]
            [clojure.java.io :as io]))

(import java.util.concurrent.Executors)
(import '[java.io StringWriter]
        '[java.net Socket])

(def ^:const varnish-exchange "varnish-exchange")

;; Purge method implements the documented method of purges on varnish,
;; https://www.varnish-cache.org/docs/4.0/users-guide/purging.html
;; Returns the first line of the response, e.g. "HTTP/1.1 200 Purged"
(defn send-purge-request
  "Sends an HTTP PURGE request to the specified host, port, and path"
  [varnishhost varnishport webhost webpath]
  (with-open [sock (Socket. varnishhost varnishport)
              out (io/writer sock)
              in (io/reader sock)]
    (.write out (str "PURGE " webpath " HTTP/1.1\n" "Host: " webhost "\n\n" ))    
    (.flush out)
    (.readLine in)))

(defn ^String extract-path
  "Extracts the path from a given message payload, can be changed
  to accept e.g. json as input"
  [^String payload]
   (String. payload "UTF-8"))

;; FIXME allow also bans based on regex
(defn handle-message
  "Handles an incoming message from the queue"
  [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
  (let [path (extract-path payload)]
    (log/debug (format "Asked to purge: %s" path))
    ;; FIXME Make configurable through command line arguments
    (send-purge-request "127.0.0.1" 6081 "example.com" path)
    (lb/ack ch delivery-tag)))

(defn start-subscriber
  "Starts a consumer by creating a queue for this consumer and binding
  it too the varnish-exchange"
  [ch queue-name routing-key]
  (log/info (format "Starting subscriber with queue %s" queue-name))
  (lq/declare ch queue-name {:exclusive true :auto-delete true})
  (lq/bind ch queue-name varnish-exchange {:routing-key routing-key})
  (lc/subscribe ch queue-name handle-message))

(defn connect-and-subscribe
  [options]
  (let [subscriber-queue (str "varnish-queue-" (options :node))
        conn (rmq/connect {:username (options :queue-user)
                           :password (options :queue-password)
                           :executor (Executors/newFixedThreadPool (options :threads))})
        ch (lch/open conn)
        routing-key "varnish-routing-key"] 
    (log/info (format "Connected. Channel id: %d" (.getChannelNumber ch)))
    (le/declare ch varnish-exchange "topic" {:durable false :auto-delete true})    
    (start-subscriber ch subscriber-queue routing-key)))

(def cli-options
  [["-n" "--node NODE" "Varnish Node Number"
    :default 1
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]

   ;; FIXME not used yet, see above
   ;; ["-h" "--host webhost" "Host sent as part of the purge request"
   ;; :default "example.com"]
   
   ["-qu" "--queue-user user" "Username used to authenticate on rabbitmq" 
    :default "admin"]
   
   ["-qp" "--queue-password password" "Password used to authenticate on rabbitmq"
    :default "nimda"]
   
   ["-t" "--threads threads" "Number of threads used to consume"
    :default 16]])

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      errors (exit 1 (error-msg errors)))
    (connect-and-subscribe options)))
