(ns cib.util
  (:import
    (java.nio
      ByteBuffer)
    (java.nio.file
      Path
      Paths)
    (java.util
      Base64
      Base64$Decoder)
    (com.google.cloud.tools.jib.api RelativeUnixPath AbsoluteUnixPath)))


(set! *warn-on-reflection* true)


(defn ^Path path
  [& more]
  (Paths/get (first more)
             (into-array String (rest more))))

(defn relative-unix-path
  [path]
  (RelativeUnixPath/get path))

(defn absolute-unix-path
  [path]
  (AbsoluteUnixPath/get path))

(defn base64-decode
  ([encoded] (base64-decode encoded nil))
  ([encoded type]
   (let [decoder ^Base64$Decoder (case type
                                   :url (Base64/getUrlDecoder)
                                   :mime (Base64/getMimeDecoder)
                                   (Base64/getDecoder))]
     (cond
       (instance? ByteBuffer encoded)
       (.decode decoder ^ByteBuffer encoded)
       (instance? String encoded)
       (.decode decoder ^String encoded)
       :else (.decode decoder ^bytes encoded)))))
