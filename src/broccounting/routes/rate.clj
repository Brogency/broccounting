(ns broccounting.routes.rate
  (:require [compojure.core :refer [GET POST defroutes]]
            [ring.util.response :refer [response content-type]]
            [broccounting.views.layout :as layout]
            [broccounting.models.rate :as rate]))


(defn rates [session]
  (let [rate-db (rate/rate-db session)]
    (layout/common [:h2 "Rates:"]
                   [:div (layout/rates-form rate-db)])))

(defn update-rates [form-data session]
  (let [rate-db (rate/form-data->rate-db form-data)
        session (rate/set-rates session rate-db)
        html-data (rates session)
        resp (content-type (response html-data) "text/html; charset=utf-8")
        resp (assoc resp :session session)]
    resp))

(defroutes rate-routes
  (GET  "/rates" [:as {session   :session}] (rates session))
  (POST "/rates" [:as {session   :session
                       form-data :form-params}]
    (update-rates form-data session)))
