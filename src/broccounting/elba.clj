(ns broccounting.elba
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [ring.util.codec :refer [url-encode]]))

(defn create-bill [login password bill]
  (client/post 
   "https://elba.kontur.ru/API/CreateBill.ashx"
   {:body (generate-string bill)
    :throw-exceptions false
    :headers {"X-Login"    (url-encode login)
              "X-Password" (url-encode password)}}))

(defn build-item [[id text _user quantity price total]]
  {:ProductName (str id " " text)
   :UnitName "час"
   :Quantity quantity
   :Price price
   :PriceWithoutNds price
   :Sum total})

(defn build-bill [name contractor items]
  {:Number name
   :Date (f/unparse (f/formatters :date-time) (t/now)) ;;"2014-02-07T00:00:00.000Z",
   :WithNds false
   :SumsWithNds false
   :Contractor {:Name contractor}
   :Items (map build-item items)})
