(ns broccounting.routes.home
  (:require [compojure.core :refer :all]
            [ring.util.response :refer [redirect]]
            [broccounting.views.layout :as layout]
            [clojure.xml :as xml]
            [clj-http.client :as client]))

(defn home [request]
  (layout/common 
    [:div
      [:h1 "Hello World!"]
      (str request)
      [:br ]
      [:a {:href "/tasks"} "login"]]))

(defn login [session]
      (layout/common 
          [:div
            [:h3 "Result"]
            [:form {:method "POST"}
             [:input {:name "login"}]
             [:input {:name "password" :type "password"}]
             [:input {:type "submit"}]]]))

(defn login-do-login [login password session]
  (let [connect-result (client/post "http://bro.myjetbrains.com/youtrack/rest/user/login"
                                    {:throw-exceptions false
                                     :form-params {:login login
                                                   :password password}})]
      (if (= (:body connect-result) "<login>ok</login>")
        (let [JSESSIONID (:value (get (:cookies connect-result) "JSESSIONID"))
              session (assoc session :jsessionid JSESSIONID)]
          (-> (redirect "/projects")
              (assoc :session session)))
        (layout/error (:body connect-result)))))

(defn youtrack-query [path session & [opts]]
  (client/get (str "http://bro.myjetbrains.com/youtrack/rest/" path)
              (merge 
                {:cookies {"JSESSIONID" {:value (:jsessionid session)}}}
                opts)))


(defn projects [session]
  (let [response (youtrack-query "admin/project" session)
        xml-data (:body response)
        stream (java.io.ByteArrayInputStream. (.getBytes (.trim xml-data)))]
    (layout/common [:h2 "Projects:"]
                   [:div (map (fn [t] [:p (:id (:attrs t))]) (:content (xml/parse stream)))])))

 
(defroutes home-routes
  (GET "/" request (home request))
  (GET "/login" [:as {session :session}] (login session))
  (POST "/login" [login password :as {session :session}]
        (login-do-login login password session)))

(defmacro def-private-routes [name guard & routes]
  `(defroutes ~name 
     ~@(for [route routes
             :let [[method path destruct call] route]]
         `(~method ~path [request#] 
                   (fn [request#] 
                     (if (~guard request#)
                       (let [~destruct request#] ~call)
                       (redirect "/login")))))))


(defn guard [request]
  (let [[:as {session :session}] request
        response (youtrack-query 
                   "admin/user/root" 
                   session 
                   {:throw-exceptions false})]
    (= (:status response) 200)))

(def-private-routes project-home-routes guard
  (GET "/projects" [:as {session :session}] (projects session)))
