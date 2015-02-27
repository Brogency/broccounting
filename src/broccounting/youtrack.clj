(ns broccounting.youtrack
  (:require [compojure.core :refer [defroutes routes let-request]]
            [clojure.xml :as xml]
            [clojure.data.csv :as csv]
            [ring.util.response :refer [redirect]]
            [clj-http.client :as client]))

(declare ^:dynamic *youtrack-entrypoint*)
(declare ^:dynamic *request-cookie-store*)

(defmacro with-youtrack [entripoint & code]
  `(binding [*youtrack-entrypoint* ~entripoint]
     ~@code))

(defmacro with-cookie-store [cs & code]
  `(binding [*request-cookie-store* ~cs]
     ~@code))

(defn- parse-xml [xml-data]
  (let [stream (java.io.ByteArrayInputStream. (.getBytes (.trim xml-data)))
        body (xml/parse stream)]
    body))

(defn- query [method path session & [opts]]
  (let [response (method 
                  (str *youtrack-entrypoint* path)
                  (merge 
                   {:cookie-store *request-cookie-store* 
                    :throw-exceptions false}
                   opts))
        responce-body (:body response)
        content-type-header ((:headers response) "Content-Type")
        content-type (get (clojure.string/split content-type-header #";") 0)

        parser ({"text/csv" csv/read-csv
                 "application/xml" parse-xml}
                content-type
                identity)
        body (parser responce-body)]
    (assoc response :body body))) 

(defn post [path session & [opts]]
  (query client/post path session opts))

(defn get [path session & [opts]]
  (query client/get path session opts))
