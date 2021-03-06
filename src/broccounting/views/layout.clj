(ns broccounting.views.layout
  (:require [hiccup.page :refer [html5 include-css]]))

(defn common [& body]
  (html5
   [:head
    [:title "Welcome to broccounting"]
    (include-css "/css/screen.css")]
   [:body body]))

(defn error [& body]
  (common [:div {:class "error"} body]))

(defn display-matrix [matrix]
  [:table
   (for [row matrix]
     [:tr
      (for [item row]
        [:td (str item)])])])

(defn history-list [history]
  [:ul
   (for [item history]
     [:li [:a {:href (str "/report/" item)} item]])])

(defn projects [projects]
  [:div
   (for [project-id projects]
     [:p [:a
          {:href (str "/project/" project-id)}
          project-id]])])

(defn rates-form [rates-db]
  [:form {:method "POST"}
   (for [[name rate] rates-db]
     [:p [:label name] [:input {:name name :value rate}]])
   [:p [:input {:type "Submit" :value "Update rate"}]]])


(defn display-rate-db [rate-db]
  [:ul
   (for [[name rate] rate-db]
     [:li (str name " ") [:strong rate]])
   [:li [:a {:href "/rates"} "Chage rates"]]])
   
