(ns broccounting.routes.report
  (:require [compojure.core :refer [GET POST defroutes]]
            [ring.util.response :refer [redirect response content-type]]
            [broccounting.views.layout :as layout]
            [broccounting.models.history :as history]
            [broccounting.models.report :as report]
            [broccounting.models.rate :as rate]
            [broccounting.models.elba :refer [credentials]]
            [broccounting.youtrack :as youtrack]
            [broccounting.elba :as elba]))

(defn reports [session]
  (layout/common [:h2 "Reports"]
                 [:h3 "Last reports"]
                 (layout/history-list (history/history session))
                 [:form {:method "POST"}
                  [:input {:name "report_id" :type "text"}]
                  [:input {:type "submit" :value "new report"}]]))

(defn- get-full-report [report_id session]
 (let [resp (youtrack/get (str "current/reports/" report_id "/export") session)]
    (if (= 200 (:status resp))
      (let [report (report/minify (rest (:body resp)))
            report-users (report/table->users report)
            report-data (report/table->hash report)
            full-report (report/hash->table report-data (rate/rate-db session))]
        [report-users full-report])
      nil)))


(defn report [report_id session]
      (let [[report-users full-report] (get-full-report report_id session)]
        (if (nil? full-report)
          (redirect "/reports")
          (let [session (history/update session report_id)
                session (rate/add-users session report-users)
                html-data (layout/common
                           [:h2 "Project " [:strong report_id]]
                           (layout/display-matrix full-report)
                           (layout/display-rate-db (rate/rate-db session))
                           [:form {:method :POST}
                            [:p [:label "Contractor:"] [:input {:name "contractor"}]]
                            [:p [:label "Bill name:"] [:input {:name "bill_name"}]]
                            [:p [:label "Elba login:"] [:input {:name "elba_login"}]]
                            [:p [:label "Elba password:"] [:input {:name "elba_password" :type :password}]]
                            [:input {:type :submit}]])
                resp (content-type (response html-data)  "text/html; charset=utf-8")
                resp (assoc resp :session session)]
            resp))))


(defn create-bill [report_id session form-data]
  (let [[_ full-report] (get-full-report report_id session)
        login (:elba_login form-data)
        password (:elba_password form-data)
        bill (elba/build-bill (:bill_name form-data) (:contractor form-data) full-report)
        result (elba/create-bill login password bill)]
    (layout/common result)))

(defroutes report-routes
  (GET  "/reports"    [:as {session :session}]    (reports session))
  (POST "/reports"    [report_id]                 (redirect (str "report/" report_id)))
  (GET  "/report/:id" [id :as {session :session}] (report id session))
  (POST "/report/:id" [id :as {session :session params :params}] (create-bill id session (dissoc params :id))))
