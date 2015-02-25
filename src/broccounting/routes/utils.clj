(ns broccounting.routes.utils
  (:require [compojure.core :refer [defroutes let-request]]
            [ring.util.response :refer [redirect]]
            [broccounting.youtrack :as youtrack]))

(defn default-guard [request]
  (let-request [[:as {session :session}] request]
               (let [res (youtrack/get
                          "admin/user/root"
                          session)]
                 (= (:status res) 200))))

(defmacro def-private-routes [name guard & routes]
  `(defroutes ~name 
     ~@(for [route routes
             :let [[method path destruct call] route]]
         `(~method ~path [request#] 
                   (fn [request#] 
                     (if (~guard request#)
                       (let-request [~destruct request#] ~call)
                       (redirect "/login")))))))
