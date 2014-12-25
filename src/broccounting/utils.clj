(ns broccounting.utils
  (:require [compojure.core :refer [defroutes routes]]
            [clojure.xml :as xml]
            [ring.util.response :refer [redirect]]
            [clj-http.client :as client]))

(defn- youtrack-query [method path session & [opts]]
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
        xml-data (:body response)
        stream (java.io.ByteArrayInputStream. (.getBytes (.trim xml-data)))
        body (xml/parse stream)]
  (assoc response :body body))) 

(defn youtrack-post [path session & [opts]]
  (youtrack-query client/post path session opts))
 
(defn youtrack-get [path session & [opts]]
  (youtrack-query client/get path session opts))
 
(defmacro def-private-routes [name guard & routes]
  `(defroutes ~name 
     ~@(for [route routes
             :let [[method path destruct call] route]]
         `(~method ~path [request#] 
                   (fn [request#] 
                     (if (~guard request#)
                       (let [~destruct request#] ~call)
                       (redirect "/login")))))))
