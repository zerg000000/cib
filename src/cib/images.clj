(ns cib.images
  (:require
    [cib.util :as util])
  (:import
    (com.google.cloud.tools.jib.api
      Containerizer
      Credential
      CredentialRetriever
      DockerDaemonImage
      RegistryImage
      TarImage)
    (java.util
      Optional)))


(set! *warn-on-reflection* true)


(def aws-enabled?
  (try
    (require 'cognitect.aws.client.api)
    true
    (catch Throwable _ false)))


(defn daemon
  [image-ref]
  (DockerDaemonImage/named ^String image-ref))


(defn tar
  [{:keys [at image-ref]}]
  (cond-> (TarImage/at (util/path at))
    image-ref
    (.named ^String image-ref)))


(defn basic
  "Return Basic Credential for Registry"
  [^String username ^String password]
  (reify CredentialRetriever
    (retrieve
      [_]
      (Optional/of (Credential/from username password)))))


(defn oauth-refresh-token
  [^String token]
  "Return Oauth 2 Refresh Token Credential for Registry"
  (reify CredentialRetriever
    (retrieve
      [_]
      (Optional/of (Credential/from "<token>" token)))))


(defn ^:dynamic aws-ecr
  "Return AWS ECR Credential for Registry"
  [& registryIds]
  {:pre [aws-enabled?]}
  (reify CredentialRetriever
    (retrieve
      [_]
      (let [ecr ((ns-resolve (symbol "cognitect.aws.client.api") (symbol "client"))
                 {:api :ecr})
            token-info (->> ((ns-resolve (symbol "cognitect.aws.client.api") (symbol "invoke"))
                             ecr (cond-> {:op :GetAuthorizationToken}
                                   (seq registryIds)
                                   (assoc :request {:registryIds registryIds})))
                            :authorizationData
                            first)

            token (-> token-info
                      :authorizationToken
                      ^bytes (util/base64-decode)
                      (String.)
                      (subs 4))]
        (Optional/of (Credential/from "AWS" token))))))


(defn registry
  [{:keys [image-ref auth]}]
  (cond-> (RegistryImage/named ^String image-ref)
    auth (.addCredentialRetriever auth)))


(defprotocol ImageStore

  (to [this]))


(extend-protocol ImageStore
  TarImage
  (to [this] (Containerizer/to this))
  DockerDaemonImage
  (to [this] (Containerizer/to this))
  RegistryImage
  (to [this] (Containerizer/to this)))
