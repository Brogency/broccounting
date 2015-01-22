(ns broccounting.models.rate)

(defn get-rate [session] 
  (:rate-db session {}))

(defn set-rate [session user & [rate]]
  (assoc-in session [:rate-db user] (or rate 0)))

(defn add-user [session user]
  (update-in session [:rate-db user] #(or % 0)))

(defn add-users [session user-list]
  (reduce add-user session user-list))
