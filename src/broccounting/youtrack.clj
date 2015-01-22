(ns broccounting.youtrack
  (:require [compojure.core :refer [defroutes routes let-request]]
            [clojure.xml :as xml]
            [clojure.data.csv :as csv]
            [ring.util.response :refer [redirect]]
            [clj-http.client :as client]))

(defn- parse-xml [xml-data]
  (let [stream (java.io.ByteArrayInputStream. (.getBytes (.trim xml-data)))
        body (xml/parse stream)]
    body))

(defn- query [method path session & [opts]]
  (let [jsessionid (:jsessionid session)
        cookies (if jsessionid 
                  {"JSESSIONID" {:value jsessionid}}
                  {})
        response (method 
                   (str "http://bro.myjetbrains.com/youtrack/rest/" path)
                   (merge 
                     {:cookies cookies 
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
