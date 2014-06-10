(defproject moogus-queue "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.4"]
                 [clj-http "0.9.2"]
                 [liberator "0.11.0"]
                 [com.datomic/datomic-free "0.9.4766"
                  :exclusions [org.jboss.logging/jboss-logging org.jgroups/jgroups]
                  ]
                 [org.immutant/immutant "1.1.2"
                  :exclusions [org.hornetq/hornetq-core-client io.netty/netty]]
                 #_
                 [ring "1.3.0" :exclusions [hiccup]]
                 [compojure "1.1.8"]]

  :pedantic? :warn ; :abort

  :immutant {:init moogus-queue/init
             :resolve-dependencies true
             :context-path "/"}

  
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.4"]]
                   :source-paths ["dev"]}}

  :plugins [[lein-cloverage "1.0.2"]
            [lein-immutant "1.2.1"]]
  )
