(ns broccounting.routes.project
  (:require [compojure.core :refer :all]
            [clj-time.core :as t]
            [broccounting.views.layout :as layout]
            [broccounting.utils :refer [youtrack-get def-private-routes]]))
  
(defn projects [session]
  (let [response (youtrack-get "admin/project" session)
        body (:body response)]
    (layout/common [:h2 "Projects:"]
                   [:div
                    (for [project-id (map (comp :id :attrs) (:content body))]
                      [:p [:a
                           {:href (str "/project/" project-id)}
                           project-id]])])))

(defn project [project_id session]
  (let [now (t/now)
        year (t/year now)
        month (t/month now)
        day (t/day now)
        start-period (t/local-date year month 1)
        stop-period (t/local-date year month day)
        report (youtrack-get "current/reports/87-50/export" session)]
    (layout/common [:h2 "Project "
                        [:strong project_id]]
                   [:p "Report period"]
                   [:p "From " start-period " To " stop-period]
                   [:table
                    (for [row (:body report)]
                      [:tr
                       (for [item row]
                         [:td item])])])))

(defn guard [request]
  (let [[:as {session :session}] request
        response (youtrack-get 
                   "admin/user/root" 
                   session)]
    (= (:status response) 200)))

(def-private-routes project-routes guard
  (GET "/projects" [:as {session :session}] (projects session))
  (GET "/project/:id" [:as {{id :id} :params session :session}] (project id session)))
