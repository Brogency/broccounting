(ns broccounting.routes.home
  (:require [compojure.core :refer :all]
            [broccounting.views.layout :as layout]
            [clj-http.client :as client]))

(defn home []
  (layout/common [:h1 "Hello World!"]))

(defn tasks []
  (layout/common 
      [:div
        [:h3 "Result"]
        [:form {:method "POST"}
         [:input {:name "login"}]
         [:input {:name "password" :type "password"}]
         [:input {:type "submit"}]]]))

(defn tasks-login [login password]
  (let [connect-result (client/post "http://bro.myjetbrains.com/youtrack/rest/user/login"
                                    {:throw-exceptions false
                                     :form-params {:login login
                                                   :password password}})]
    (layout/common 
      (if (= (:body connect-result) "ok")
        "ok"
        (:body connect-result)))))


(defroutes home-routes
  (GET "/" [] (home))
  (GET "/tasks" [] (tasks))
  (POST "/tasks" [login password] (tasks-login login password)))

