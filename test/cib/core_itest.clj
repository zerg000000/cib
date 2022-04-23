(ns cib.core-itest
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [cib.core :as cib]
            [cib.images :as images]
            [clj-test-containers.core :as tc]
            [contajners.core :as c]
            [clojure.java.io :as io]))

(def image-ref "test-project:0.0.1")

(def image-spec
  #:jib.java{:jib/from "gcr.io/distroless/java17-debian11:debug"
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

(defn load-image-from-tar [f]
  (let [client (c/client {:engine   :docker
                          :category :images
                          :version  "v1.41"
                          :conn     {:uri "unix:///var/run/docker.sock"}})]
    (c/invoke client {:op   :ImageLoad
                      :data (io/input-stream (io/file f))})))


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
      (tc/stop! container)))
  (testing "tar build"
    (-> (cib/java-container image-spec)
        (cib/containerize (images/tar {:at "test.tar" :image-ref image-ref})))
    (load-image-from-tar "test.tar")
    (let [container (-> (tc/create {:image-name image-ref
                                    :exposed-ports [8090]})
                        (tc/start!))
          host (:host container)
          port (get-in container [:mapped-ports 8090])]
      (Thread/sleep 5000)
      (is (= "OK" (slurp (str "http://" host ":" port)))
          "built image is runnable")
      (tc/stop! container)))
  (testing "arm64 build"
    (-> (cib/java-container (assoc image-spec :jib/platforms #{[:arm64 :linux]}))
        (cib/containerize (images/daemon image-ref)))
    (let [container (-> (tc/create {:image-name image-ref
                                    :exposed-ports [8090]})
                        (tc/start!))
          host (:host container)
          port (get-in container [:mapped-ports 8090])]
      (Thread/sleep 5000)
      (is (= "OK" (slurp (str "http://" host ":" port)))
          "built image is runnable")
      (tc/stop! container)))
  (testing "amd64 build"
    (-> (cib/java-container (assoc image-spec :jib/platforms #{[:amd64 :linux]}))
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

