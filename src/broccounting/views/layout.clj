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
