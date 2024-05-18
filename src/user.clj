(ns user
 (:require
  [clojure.string :as str]
  [ring.adapter.jetty :as jetty]
  [steffan-westcott.clj-otel.api.trace.span :as span]
  [steffan-westcott.clj-otel.context :as context]
  [taoensso.timbre :as log]))

(defn- handler
 [_]
 (println "OUT SPAN" (context/current))
 (span/with-span! {:name "TEST"}
   (println "IN SPAN" (context/current))))

(defn- otel-context-middleware
 [{:keys [?ns-str] :as data}]
 (when-not (str/starts-with? ?ns-str "io.opentelemetry.javaagent.")
   (span/get-span-context))
 data)

(defn -main
 []
 (log/merge-config! {:min-level :debug
                     ;; With this middleware and `otel.javaagent.logging=application`
                     ;; clj-otel cannot see agent-generated spans:
                     :middleware [otel-context-middleware]})
 (jetty/run-jetty #'handler {:port 3000}))
