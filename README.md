# Cib

![CI](https://github.com/zerg000000/cib/actions/workflows/ci.yaml/badge.svg?branch=master)

Cib builds optimized Docker and OCI images for your clojure application using Jib.

* Wrappers for Jib building blocks
* Data Driven

## Installation

Currently, only support via git deps

```clojure
{zerg000000/cib {:git/url "https://github.com/zerg000000/cib" 
                 :sha "c934e91d9d1d0f5f66b08c8d78278a1087e4deb6"}}
```

## Quickstart

```clojure
(require '[cib.core :as cib]
         '[cib.images :as images]
         '[cib.specs]
         '[clojure.spec.alpha :as s])

(def config #:jib.java{:jib/from "gcr.io/distroless/java:11"
                       :jib/exposed-ports [[:tcp 3000]]
                       :dependencies ["target/lib/a-lib.jar"]
                       :classes "target/classes"
                       :resources "src"
                       :main-class 'app.main
                       :jvm-flags ["-Xmx512m" "-Xms256m"]})

(s/explain-data :jib.container/java config)

(-> (cib/java-container config)
    (cib/containerize (images/daemon "cib:0.0.1")))
```


## Special Feature (arm64 build)

You don't need a M1 to build arm docker image or using extremely slow buildx for it.
Just make a small change in config. Done!

```clojure
#:jib.java{:jib/from "gcr.io/distroless/java:11"
           :jib/exposed-ports [[:tcp 3000]]
           :dependencies ["target/web-standalone.jar"]
           :main-class 'app.main
           :jib/platforms #{[:arm64 :linux]}
           :jvm-flags ["-Xmx512m" "-Xms256m"]}
```

## License

Copyright (c) 2018-2022 Albert Lai

Distributed under the Eclipse Public License 2.0.
