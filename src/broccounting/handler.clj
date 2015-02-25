(ns broccounting.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [broccounting.routes.home :refer [home-routes]]
            [broccounting.routes.project :refer [project-routes]]
            [broccounting.routes.rate :refer [rate-routes]]
            [broccounting.routes.report :refer [report-routes]]))

(defn init []
  (println "broccounting is starting"))

(defn destroy []
  (println "broccounting is shutting down"))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (routes home-routes project-routes rate-routes report-routes app-routes)
      (wrap-session 
       {:store (cookie-store {:key "1234567890123456"})})
      (handler/site)
      (wrap-base-url)))
