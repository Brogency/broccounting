(ns broccounting.utils
  (:require [compojure.core :refer [defroutes routes]]
            [clojure.xml :as xml]
            [clojure.data.csv :as csv]
            [ring.util.response :refer [redirect]]
            [clj-http.client :as client]))

(defn- parse-xml [xml-data]
  (let [stream (java.io.ByteArrayInputStream. (.getBytes (.trim xml-data)))
        body (xml/parse stream)]
    body))

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
        responce-body (:body response)
        content-type-header ((:headers response) "Content-Type")
        content-type (get (clojure.string/split content-type-header #";") 0)

        parser ({"text/csv" csv/read-csv
                 "application/xml" parse-xml}
                content-type
                identity)
        body (parser responce-body)]
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

(defn transform-report [report]
  (for [real-row report
        :let [row [(get real-row 6)
                   (get real-row 7)
                   (get real-row 2)
                   (get real-row 4)]]]
    row))

; {:OCSIAL-1
;  {:name "Публикация новостей в соц сети"
;   :participants {:ir4y 10}}}

(defn parse-int [s]
    (Integer/parseInt (re-find #"\A-?\d+" s)))

(defn group-report-result [report]
  (reduce (fn [result row]
            (let [row (first report)
                  task-id (keyword (first row))
                  task-name (get row 1)
                  spent-time (parse-int (get row 2))
                  username (keyword (last row))]
              (if (contains? result task-id)
                (if (contains? (:participants (task-id result)) username)
                   (update-in result [task-id :participants username] #(+ spent-time %))
                   (assoc-in result [task-id :participants] {username spent-time}))
                (assoc result task-id {:name task-name
                                       :participants {username spent-time}}))))
          {}
          report))
