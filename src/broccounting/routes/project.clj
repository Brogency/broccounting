(ns broccounting.routes.project
  (:require [compojure.core :refer [GET]]
            [broccounting.views.layout :as layout]
            [broccounting.models.project :as project]
            [broccounting.routes.utils :refer :all]
            [broccounting.youtrack :as youtrack]))

  
(defn projects [session]
  (let [resp (youtrack/get "admin/project" session)
        body (:body resp)
        projects (project/xml->project_ids (:content body))]
    (layout/common [:h2 "Projects:"]
                   (layout/projects projects))))

(defn project [project_id session]
  (layout/common [:h2 (str "Project: " project_id)]
                 [:div "Content come soon"]
                 [:a {:href "/projects"} "<- back"]))


(def-private-routes project-routes default-guard
  (GET "/projects" [:as {session :session}] (projects session))
  (GET "/project/:id" [:as {{id :id} :params session :session}] (project id session)))
