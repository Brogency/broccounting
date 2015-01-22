(ns broccounting.models.history)

(defn history [session]
 (:history session []))

(defn push-to-history [item len col]
  (vec
    (if (some #(= item %) col)
      col
      (take len (conj col item)))))

(defn update [session item]
  (update-in session [:history] (partial push-to-history item 10)))
