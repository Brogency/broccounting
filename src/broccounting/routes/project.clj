(ns broccounting.routes.project
  (:require [compojure.core :refer :all]
            [ring.util.response :refer [redirect response content-type]]
            [clj-time.core :as t]
            [broccounting.views.layout :as layout]
            [broccounting.utils :refer :all]))

  
(defn projects [session]
  (let [resp (youtrack-get "admin/project" session)
        body (:body resp)]
    (layout/common [:h2 "Projects:"]
                   [:div
                    (for [project-id (map (comp :id :attrs) (:content body))]
                      [:p [:a
                           {:href (str "/project/" project-id)}
                           project-id]])])))

(defn project [project_id session]
  (layout/common [:h2 (str "Project: " project_id)]
                 [:div "Content come soon"]
                 [:a {:href "/projects"} "<- back"]))


(defn reports [session]
  (let [history (:history session [])]
    (layout/common [:h2 "Reports"]
                   [:h3 "Last reports"]
                   [:ul
                    (for [item history]
                      [:li [:a {:href (str "/report/" item)} item]])]
                   [:form {:method "POST"}
                    [:input {:name "report_id" :type "text"}]
                    [:input {:type "submit" :value "new report"}]])))

(defn report [report_id session]
  (let [resp (youtrack-get (str "current/reports/" report_id "/export") session)]
    (if (= 200 (:status resp))
      (let [report (transform-report (rest (:body resp)))
            group-report (group-report-result report)
            html-data (layout/common
                        [:h2 "Project "
                         [:strong report_id]]
                        (layout/display-matrix
                          (transform-group-report group-report {})))
            session (update-in session [:history] (partial push-to-history report_id 10))
            resp (content-type (response html-data)  "text/html; charset=utf-8")
            resp (assoc resp :session session)]
        resp)
      (redirect "/reports"))))

(defn guard [request]
  (let-request [[:as {session :session}] request]
    (let [res (youtrack-get
                "admin/user/root"
                session)]
    (= (:status res) 200))))

(def-private-routes project-routes guard
  (GET "/projects" [:as {session :session}] (projects session))
  (GET "/project/:id" [:as {{id :id} :params session :session}] (project id session))
  (GET "/reports" [:as {session :session}] (reports session))
  (POST "/reports" [report_id] (redirect (str "report/" report_id)))
  (GET "/report/:id" [id :as {session :session}] (report id session)))
