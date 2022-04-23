(ns build
  (:require
    [clojure.tools.build.api :as b]
    [org.corfield.build :as bb]))


(def lib 'test/web)
(def version (format "1.0.%s" (b/git-count-revs nil)))
(def main 'test-project.main)


(defn uber
  [opts]
  (-> opts
      (assoc :lib lib :main main)
      (bb/clean)
      (bb/uber)))
