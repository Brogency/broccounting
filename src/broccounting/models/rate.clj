(ns broccounting.models.rate
  (:require [broccounting.models.utils :refer [parse-int]]))

(defn rate-db [session]
  (:rate-db session {}))

(defn set-rate [session [user rate]]
  (assoc-in session [:rate-db user] rate))

(def set-rates (partial reduce set-rate))

(defn add-user [session user]
  (update-in session [:rate-db user] #(or % 0)))

(def add-users (partial reduce add-user))

(defn form-data->rate-db [form-data]
  (zipmap (map keyword (keys form-data)) (map parse-int (vals form-data))))
