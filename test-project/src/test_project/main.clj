(ns test-project.main
  (:gen-class)
  (:require [org.httpkit.server :as s]))

(defn -main [& args]
  (s/run-server (fn [_]
                  {:status 200
                   :body "OK"})
                args))
