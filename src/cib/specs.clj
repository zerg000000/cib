(ns cib.specs
  (:require
    [clojure.java.io :as io]
    [clojure.spec.alpha :as s]))


(set! *warn-on-reflection* true)


(defn file-exist?
  [f]
  (.exists (io/file f)))


(defn directory?
  [f]
  (.isDirectory (io/file f)))


(s/def :java.file/dir
  (s/and string?
         file-exist?
         directory?))


(s/def :java.file/file
  (s/and string?
         file-exist?))


(s/def :jib.java/dependencies
  (s/+ :java.file/dir))


(s/def :jib.java/snapshot-dependencies
  (s/+ :java.file/dir))


(s/def :jib.java/project-dependencies
  (s/+ :java.file/dir))


(s/def :jib.java/classes :java.file/dir)
(s/def :jib.java/resources :java.file/dir)
(s/def :jib.java/classpath :java.file/dir)


(s/def :jib.java/jvm-flags
  (s/+ string?))


(s/def :jib.java/main-class symbol?)
(s/def :jib.java/app-root string?)
(s/def :jib.java/classes-destination string?)
(s/def :jib.java/resources-destination string?)
(s/def :jib.java/dependencies-destination string?)
(s/def :jib.java/others-destination string?)


(s/def :jib.container/java
  (s/merge
    :jib.container/base
    (s/keys :opt [:jib.java/dependencies
                  :jib.java/snapshot-dependencies
                  :jib.java/project-dependencies
                  :jib.java/classes
                  :jib.java/resources
                  :jib.java/classpath
                  :jib.java/jvm-flags
                  :jib.java/main-class
                  :jib.java/app-root
                  :jib.java/classes-destination
                  :jib.java/resources-destination
                  :jib.java/dependencies-destination
                  :jib.java/others-destination])))


(s/def :jib.layer/files
  (s/or :dir :java.file/dir
        :file :java.file/file))


(s/def :jib.layer/path-in-container
  string?)


(s/def :jib/layer
  (s/keys :req-un [:jib.layer/files
                   :jib.layer/path-in-container]))


(s/def :jib.port/port int?)
(s/def :jib.port/protocol #{:tcp :udp})


(s/def :jib/port
  (s/tuple :jib.port/protocol :jib.port/port))


(s/def :jib/from string?)


(s/def :jib/layers
  (s/+ :jib/layer))


(s/def :jib/user string?)
(s/def :jib/working-directory :java.file/dir)
(s/def :jib/entrypoint (s/+ string?))
(s/def :jib/program-arguments (s/+ string?))
(s/def :jib/environment (s/map-of string? string?))
(s/def :jib/exposed-ports (s/* :jib/port))
(s/def :jib/labels (s/* string?))
(s/def :jib/volumes (s/* string?))
(s/def :jib/format #{:oci :docker})


(s/def :jib.container/base
  (s/keys
    :req [:jib/from]
    :opt [:jib/layers
          :jib/user
          :jib/working-directory
          :jib/entrypoint
          :jib/program-arguments
          :jib/environment
          :jib/exposed-ports
          :jib/labels
          :jib/volumes
          :jib/format]))
