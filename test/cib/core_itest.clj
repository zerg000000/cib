(ns cib.core-itest
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [cib.core :as cib]
            [cib.images :as images]
            [clj-test-containers.core :as tc]
            [contajners.core :as c]))

(def image-ref "test-project:0.0.1")

(def image-spec
  #:jib.java{:jib/from "gcr.io/distroless/java17:nonroot"
             :jib/exposed-ports [[:tcp 8090]]
             :dependencies ["test-project/target/web-standalone.jar"]
             :main-class 'test_project.main
             :jvm-flags ["-Xmx512m" "-Xms256m"]})


(defn registry-cleanup-fixture
  [f]
  (let [client (c/client {:engine   :docker
                          :category :images
                          :version  "v1.41"
                          :conn     {:uri "unix:///var/run/docker.sock"}})]
    (c/invoke client {:op :ImageDelete
                      :params {:name image-ref
                               :force true}})
    (f)
    (c/invoke client {:op :ImageDelete
                      :params {:name image-ref
                               :force true}})))

(use-fixtures :each registry-cleanup-fixture)


(deftest simple-container-test
  (testing "local registry build"
    (-> (cib/java-container image-spec)
        (cib/containerize (images/daemon image-ref)))
    (let [container (-> (tc/create {:image-name image-ref
                                    :exposed-ports [8090]})
                        (tc/start!))
          host (:host container)
          port (get-in container [:mapped-ports 8090])]
      (Thread/sleep 5000)
      (is (= "OK" (slurp (str "http://" host ":" port)))
          "built image is runnable")
      (tc/stop! container))))

