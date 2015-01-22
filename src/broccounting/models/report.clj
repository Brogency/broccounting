(ns broccounting.models.report
  (:require [broccounting.models.utils :refer [parse-int]]))

(defn minify [report]
  (for [real-row report
        :let [row [(get real-row 6)
                   (get real-row 7)
                   (get real-row 2)
                   (get real-row 4)]]]
    row))

(defn hash->table [group-report rate-db]
 (apply concat (for [[task {task-name :name
              participants :participants}] group-report]
    (for [[user spent-time] participants
          :let [user-rate (user rate-db 0)
                spent-time (float (/ spent-time 60))
                work-cost (float (* spent-time user-rate))]]
      [task task-name user spent-time work-cost]))))

; Structure of group report
; {:OCSIAL-1
;  {:name "Публикация новостей в соц сети"
;   :participants {:ir4y 10}}}
(defn report-reducer [result row]
  (let [task-id (keyword (first row))
        task-name (get row 1)
        spent-time (parse-int (get row 2))
        username (keyword (last row))]
    (if (contains? result task-id)
      (if (contains? (:participants (task-id result)) username)
         (update-in result [task-id :participants username] #(+ spent-time %))
         (let [participants (:participants (task-id result))]
           (assoc-in result [task-id :participants] (merge participants {username spent-time}))))
      (assoc result task-id {:name task-name
                             :participants {username spent-time}}))))

(defn table->hash [report]
  (reduce report-reducer {} report))

(defn user-reducer [result row]
  (let [username (keyword (last row))]
    (conj result username)))

(defn table->users [report]
  (reduce user-reducer #{} report))
