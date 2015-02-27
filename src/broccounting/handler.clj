(ns broccounting.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.util.response :refer [redirect]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [broccounting.routes.home :refer [home-routes]]
            [broccounting.routes.project :refer [project-routes]]
            [broccounting.routes.rate :refer [rate-routes]]
            [broccounting.routes.report :refer [report-routes]]
            [broccounting.youtrack :refer [with-youtrack with-cookie-store]]))

(defn init []
  (println "broccounting is starting"))

(defn destroy []
  (println "broccounting is shutting down"))

(defonce cookie-stories (atom {}))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn youtrack-middleware [handler]
  (fn [request]
    (let [youtrack ((:session request) :youtrack )
          cs-id ((:session request) :cs-id (uuid))]
      (when (nil? (get @cookie-stories cs-id))
        (swap! cookie-stories #(assoc % cs-id (clj-http.cookies/cookie-store))))
      (with-cookie-store (get @cookie-stories cs-id)
        (let [responce (if (nil? youtrack)
                         (if (= (:uri request) "/login")
                           (handler request)
                           (redirect "/login"))
                         (with-youtrack youtrack
                           (handler request)))
              session (:session responce)
              session (assoc session :cs-id cs-id)
              responce (assoc responce :session session)]
              responce)))))

(def app
  (-> (routes home-routes project-routes rate-routes report-routes app-routes)
      (youtrack-middleware)
      (wrap-session 
       {:store (cookie-store {:key "1234567890123456"})})
      (handler/site)
      (wrap-base-url)))
