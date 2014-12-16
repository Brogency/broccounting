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

(defn tasks [session]
  (let [id (:jsessionid session)]
    (if id
      (layout/common [:h3 (str "Hello " id)])
      (layout/common 
          [:div
            [:h3 "Result"]
            [:form {:method "POST"}
             [:input {:name "login"}]
             [:input {:name "password" :type "password"}]
             [:input {:type "submit"}]]]))))

(defn tasks-login [login password session]
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

(defn projects [session]
  (let [id (:jsessionid session)]
    (if id
      (let [response (client/get "http://bro.myjetbrains.com/youtrack/rest/admin/project"
                           {:response-interceptor (fn [resp ctx] (println resp))
                            :cookies {"JSESSIONID" {:value (:jsessionid session)}}})
            xml-data (:body response)
            stream (java.io.ByteArrayInputStream. (.getBytes (.trim xml-data)))]
        (layout/common [:h2 "Projects:"]
;                       [:div (str (doall (map (fn [tag] (:attrs tag)) (:content (xml/parse stream)))))]))
                       [:div (map (fn [t] [:p (:id (:attrs t))]) (:content (xml/parse stream)))]))
      (redirect "/tasks"))))

 
(defroutes home-routes
  (GET "/" request (home request))
  (GET "/tasks" [:as {session :session}] (tasks session))
  (POST "/tasks" [login password :as {session :session}]
        (tasks-login login password session))
  (GET "/projects" [:as {session :session}] (projects session)))
