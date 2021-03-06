(defproject moogus-queue "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.5"]
                 [clj-http "0.9.2"]
                 [liberator "0.11.0"]
                 [org.immutant/immutant "1.1.2"
                  :exclusions [org.hornetq/hornetq-core-client io.netty/netty]]
                 [datomic-schematode "0.1.2-RC1"]
                 [org.clojure/test.check "0.5.8"]
                 [compojure "1.1.8" :exclusions [ring/ring-core]]]

  :pedantic? :warn ; :abort

  :immutant {:init moogus-queue/init
             :config-path "moogus-queue-conf.edn"
             :resolve-dependencies true
             :context-path "/"}

  
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.4"]
                                  [ring "1.3.0" :exclusions [hiccup]]
                                  [com.datomic/datomic-free "0.9.4766"
                                   :exclusions [org.jboss.logging/jboss-logging org.jgroups/jgroups]]]
                   :source-paths ["dev"]}
             :ci-test {:immutant {:config-path "moogus-queue-conf-dist.edn"}}
             :production {:repositories [["my.datomic.com" {:url "https://my.datomic.com/repo"
                                                            :username :env/lein_datomic_repo_username
                                                            :password :env/lein_datomic_repo_password}]]
                          :dependencies [[com.datomic/datomic-pro "0.9.4766"
                                          :exclusions [org.jboss.logging/jboss-logging org.jgroups/jgroups]]]}}

  :plugins [[lein-cloverage "1.0.2"]
            [lein-immutant "1.2.1"]]
  )
