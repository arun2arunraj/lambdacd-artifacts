(defproject lambdacd-artifacts "0.1.0-SNAPSHOT"
  :description "provides a way to access build artifacts generated by a step in LambdaCD"
  :url "http://github.com/flosell/lambdacd-artifacts"
  :license {:name "Apache License, version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :deploy-repositories [["clojars" {:creds :gpg}]
                        ["releases" :clojars]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [lambdacd "0.4.3"]]
  :profiles {:dev {:dependencies [[ring-server "0.3.1"]]
                   :main lambdacd-artifacts.sample-pipeline}})