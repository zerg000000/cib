# Cib

Cib builds optimized Docker and OCI images for your clojure application using Jib.

* Wrappers for Jib building blocks
* Data Driven

## Quickstart

```clojure
(require '[cib.core :as cib]
         '[cib.images :as images]
         '[cib.specs]
         '[clojure.spec.alpha :as s])

(def config #:jib.java{:jib/from "gcr.io/distroless/java:11"
                       :jib/exposed-ports [[:tcp 3000]]
                       :dependencies ["target/lib"]
                       :classes "target/classes"
                       :resources "src"
                       :main-class 'app.main
                       :jvm-flags ["-Xmx512m" "-Xms256m"]})

(s/explain-data :jib.container/java config)

(-> (cib/java-container config)
    (cib/containerize (images/daemon "cib:0.0.1")))
```

## License

Copyright (c) 2018-2020 Albert Lai

Distributed under the Eclipse Public License 2.0.
