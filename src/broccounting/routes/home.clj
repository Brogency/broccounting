(ns broccounting.routes.home
  (:require [compojure.core :refer :all]
            [ring.util.response :refer [redirect]]
            [broccounting.views.layout :as layout]
            [broccounting.youtrack :as youtrack]))

(defn home [request]
  (layout/common 
   [:div
    [:h1 "Hello World!"]
    [:br]
    [:a {:href "/login"} "login"]]))

(defn login [session]
  (layout/common 
   [:div
    [:h3 "Result"]
    [:form {:method "POST"}
     [:label "Youtrack entry point" [:input {:name "youtrack" :value (:youtrack session "")}]]
     [:br]
     [:br]
     [:label "login" [:input {:name "login"}]]
     [:br]
     [:label "Password" [:input {:name "password" :type "password"}]]
     [:br]
     [:input {:type "submit"}]]]))

(defn login-do-login [login password youtrack session]
  (youtrack/with-youtrack youtrack
    (let [response (youtrack/post 
                    "user/login" 
                    {}
                    {:form-params {:login login
                                   :password password}})
          status (:status response)
          body (:body response)
          [content] (:content body)]
      (if (= status 200)
        (let [session (assoc session :youtrack youtrack)]
          (-> (redirect "/reports")
              (assoc :session session)))
        (layout/error (str content))))))

(defroutes home-routes
  (GET "/" request (home request))
  (GET "/login" [:as {session :session}] (login session))
  (POST "/login" [login password youtrack :as {session :session}]
    (login-do-login login password youtrack session)))
