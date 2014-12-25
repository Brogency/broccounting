(ns broccounting.routes.project
  (:require [compojure.core :refer :all]
            [broccounting.views.layout :as layout]
            [broccounting.utils :refer [youtrack-get def-private-routes]]))
  
(defn projects [session]
  (let [response (youtrack-get "admin/project" session)
        body (:body response)]
    (layout/common [:h2 "Projects:"]
                   [:div (map (fn [t] [:p (:id (:attrs t))]) (:content body))])))

(defn guard [request]
  (let [[:as {session :session}] request
        response (youtrack-get 
                   "admin/user/root" 
                   session)]
    (= (:status response) 200)))

(def-private-routes project-routes guard
  (GET "/projects" [:as {session :session}] (projects session)))
