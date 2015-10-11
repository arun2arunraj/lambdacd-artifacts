(ns lambdacd-artifacts.sample-pipeline
  (:use [compojure.core]
        [lambdacd.steps.control-flow])
  (:require [lambdacd.steps.shell :as shell]
            [lambdacd.steps.manualtrigger :as manualtrigger]
            [lambdacd.core :as lambdacd]
            [ring.server.standalone :as ring-server]
            [lambdacd.util :as util]
            [lambdacd.ui.ui-server :as ui]
            [lambdacd.steps.git :as git]
            [lambdacd.steps.support :as step-support]
            [lambdacd.runners :as runners]
            [lambdacd-artifacts.core :as artifacts]
            [ring.util.response :as resp]))

(defn ^{ :display-type :container} with-git [& steps]
  (git/with-git "git@github.com:flosell/lambdacd-artifacts" steps))

(defn produce-output [args ctx]
  (shell/bash ctx (:cwd args) "lein test2junit"))

;; TODO: upgrade to chaining-macro after release (#39)
(defn some-build-step [args ctx]
    (step-support/chain args ctx
      (produce-output)
      (artifacts/publish-artifacts (:cwd args) [#"test2junit/.*"
                                                "testdata/clojure-icon.gif"])))

(defn wait-for-interaction [args ctx]
  (manualtrigger/wait-for-manual-trigger nil ctx))

(def pipeline-structure `(
 (run wait-for-interaction)
 (with-git
   some-build-step)))

(def artifacts-path-context "/artifacts")

(defn mk-routes [pipeline-routes artifacts]
  (routes
    (GET "/" [] (resp/redirect "pipeline/"))
    (context "/pipeline" [] pipeline-routes)
    (context artifacts-path-context [] artifacts)))

(defn -main [& args]
  (let [home-dir (if (not (empty? args)) (first args) (util/create-temp-dir))
        config {:home-dir home-dir
                :artifacts-path-context artifacts-path-context}
        pipeline (lambdacd/assemble-pipeline pipeline-structure config)]
    (runners/start-one-run-after-another pipeline)
    (ring-server/serve (mk-routes (ui/ui-for pipeline)
                                  (artifacts/artifact-handler-for pipeline))
                                  {:open-browser? true
                                   :port 8081})))