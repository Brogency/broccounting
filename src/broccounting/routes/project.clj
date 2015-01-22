(ns broccounting.routes.project
  (:require [compojure.core :refer :all]
            [clj-time.core :as t]
            [broccounting.views.layout :as layout]
            [broccounting.utils :refer [youtrack-get def-private-routes
                                        transform-report group-report-result]]))

  
(defn projects [session]
  (let [response (youtrack-get "admin/project" session)
        body (:body response)]
    (layout/common [:h2 "Projects:"]
                   [:div
                    (for [project-id (map (comp :id :attrs) (:content body))]
                      [:p [:a
                           {:href (str "/project/" project-id)}
                           project-id]])])))

(defn report [report_id session]
  (let [now (t/now)
        year (t/year now)
        month (t/month now)
        day (t/day now)
        start-period (t/local-date year month 1)
        stop-period (t/local-date year month day)
        response (youtrack-get (str "current/reports/" report_id "/export") session)
        report (transform-report (rest (:body response)))
        group-report (group-report-result report)]
    (layout/common [:h2 "Project "
                        [:strong report_id]]
                   [:p "Report period"]
                   [:p "From " start-period " To " stop-period]
                   [:table
                    (for [[task {task-name :name
                                 participants :participants}] group-report]
                       (for [[user spent-time] participants
                             :let [user-rait 0
                                   work-cost (* spent-time user-rait)]]
                         [:tr
                          [:td task] [:td task-name]
                          [:td user] [:td spent-time]
                          [:td work-cost]]))])))




(defn guard [request]
  (let [[:as {session :session}] request
        response (youtrack-get 
                   "admin/user/root" 
                   session)]
    (= (:status response) 200)))

(def-private-routes project-routes guard
  (GET "/projects" [:as {session :session}] (projects session))
  ;(GET "/project/:id" [:as {{id :id} :params session :session}] (project id session))
  ;(GET "/reports"
  (GET "/report/:id" [:as {{id :id} :params session :session}] (report id session)))

