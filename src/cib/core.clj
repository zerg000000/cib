(ns cib.core
  (:require
    [cib.images :as images]
    [cib.util :as util])
  (:import
    (com.google.cloud.tools.jib.api
      ImageFormat
      JavaContainerBuilder
      Jib
      JibContainerBuilder
      Port)
    (java.util
      List
      Map
      Set)))


(set! *warn-on-reflection* true)


(defn ^JibContainerBuilder add-layers
  [^JibContainerBuilder container layers]
  (doseq [{:keys [files path-in-container]} layers]
    (.addLayer container ^List files ^String path-in-container)))


(defn ^JavaContainerBuilder add-classes
  [^JavaContainerBuilder container classes]
  (if (vector? classes)
    (let [[path regex-str] classes
          regex (re-pattern regex-str)]
      (.addClasses container (util/path path) (util/as-pred #(re-find regex (str %)))))
    (.addClasses container (util/path classes))))


(defn ^JavaContainerBuilder add-resources
  [^JavaContainerBuilder container resources]
  (if (vector? resources)
    (let [[path regex-str] resources
          regex (re-pattern regex-str)]
      (.addResources container (util/path path) (util/as-pred #(re-find regex (str %)))))
    (.addResources container (util/path resources))))


(defn to-port
  [[protocol port]]
  (Port/parseProtocol port (name protocol)))


(defn container
  "Wrapper for JibContainerBuilder"
  ([config] (container (Jib/from ^String (:jib/from config)) config))
  ([^JibContainerBuilder container-builder
    {:jib/keys [layers
            user
            working-directory
            entrypoint
            program-arguments
            environment
            exposed-ports
            labels
            volumes
            format]}]
   (cond-> container-builder
     layers
     (add-layers layers)
     user
     (.setUser user)
     working-directory
     (.setWorkingDirectory working-directory)
     entrypoint
     (.setEntrypoint ^List entrypoint)
     program-arguments
     (.setProgramArguments ^List program-arguments)
     environment
     (.setEnvironment ^Map environment)
     exposed-ports
     (.setExposedPorts (->> exposed-ports
                            (map to-port)
                            ^Set (set)))
     labels
     (.setLabels ^Map labels)
     volumes
     (.setVolumes ^Set volumes)
     format
     (.setFormat (if (= format :oci)
                   ImageFormat/OCI
                   ImageFormat/Docker)))))


(defn java-container
  "Wrapper for JavaContainerBuilder"
  [{:jib.java/keys [dependencies
                    snapshot-dependencies
                    project-dependencies
                    classes
                    resources
                    classpath
                    jvm-flags
                    main-class
                    app-root
                    classes-destination
                    resources-destination
                    dependencies-destination
                    others-destination]
    :as config}]
  (cond-> (JavaContainerBuilder/from ^String (:jib/from config))
    dependencies
    (.addDependencies ^List (map util/path dependencies))
    snapshot-dependencies
    (.addSnapshotDependencies ^List (map util/path snapshot-dependencies))
    project-dependencies
    (.addProjectDependencies ^List (map util/path project-dependencies))
    classes
    (add-classes classes)
    resources
    (add-resources resources)
    classpath
    (.addToClasspath ^List (mapv util/path classpath))
    jvm-flags
    (.addJvmFlags ^List jvm-flags)
    main-class
    (.setMainClass (name main-class))
    app-root
    (.setAppRoot ^String app-root)
    classes-destination
    (.setClassesDestination (util/relative-unix-path classes-destination))
    resources-destination
    (.setResourcesDestination (util/relative-unix-path resources-destination))
    dependencies-destination
    (.setDependenciesDestination (util/relative-unix-path dependencies-destination))
    others-destination
    (.setOthersDestination (util/relative-unix-path others-destination))
    true
    (-> (.toContainerBuilder) (container config))))


(defn containerize
  "Build Container to actual image"
  [^JibContainerBuilder container-builder image-store]
  (.containerize container-builder
                 (images/to image-store)))


