(ns broccounting.routes.home
  (:require [compojure.core :refer :all]
            [ring.util.response :refer [redirect]]
            [broccounting.views.layout :as layout]
            [broccounting.utils :refer [youtrack-post]]))
  
(defn home [request]
  (layout/common 
    [:div
      [:h1 "Hello World!"]
      [:br ]
      [:a {:href "/login"} "login"]]))

(defn login [session]
      (layout/common 
          [:div
            [:h3 "Result"]
            [:form {:method "POST"}
             [:input {:name "login"}]
             [:input {:name "password" :type "password"}]
             [:input {:type "submit"}]]]))

(defn login-do-login [login password session]
  (let [response (youtrack-post 
                   "user/login" 
                   {}
                   {:form-params {:login login
                                  :password password}})
        status (:status response)
        body (:body response)
        [content] (:content body)]
      (if (= status 200)
        (let [{{jsessionid :value} "JSESSIONID"} (:cookies response)
              session (assoc session :jsessionid jsessionid)]
          (-> (redirect "/projects")
              (assoc :session session)))
          (layout/error (str content )))))

(defroutes home-routes
  (GET "/" request (home request))
  (GET "/login" [:as {session :session}] (login session))
  (POST "/login" [login password :as {session :session}]
        (login-do-login login password session)))
