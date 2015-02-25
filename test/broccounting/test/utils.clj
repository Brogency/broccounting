(ns broccounting.test.utils
  (:use clojure.test
        ring.mock.request
        broccounting.utils)
  (:require
   [compojure.core :refer [GET routes]]))


(deftest youtrack-api
  (with-redefs-fn {#'clj-http.client/get (fn [path opts]
                                           {:headers {"Content-Type" "application/xml; charset=UTF-8"}
                                            :body "<data>ok</data>"})
                   #'clj-http.client/post (fn [path opts]
                                            {:headers {"Content-Type" "application/xml; charset=UTF-8"}
                                             :body "<data>ok</data>"})}
    #(do
       (testing "Simple youtrack-get"
         (is (= (youtrack-get "foo" {}) 
                {:headers {"Content-Type" "application/xml; charset=UTF-8"}
                 :body {:tag :data, :attrs nil, :content ["ok"]}})))
       (testing "Session youtrack-get"
         (is (= (youtrack-get "foo" {:foo :bar}) 
                {:headers {"Content-Type" "application/xml; charset=UTF-8"}
                 :body {:tag :data, :attrs nil, :content ["ok"]}}))
         (is (= (youtrack-get "foo" {:jsessionid {:value "JSESSIONID"}}) 
                {:headers {"Content-Type" "application/xml; charset=UTF-8"}
                 :body {:tag :data, :attrs nil, :content ["ok"]}})))
       (testing "Simple youtrack-post"
         (is (= (youtrack-post "foo" {}) 
                {:headers {"Content-Type" "application/xml; charset=UTF-8"}
                 :body {:tag :data, :attrs nil, :content ["ok"]}})))
       (testing "Session youtrack-post"
         (is (= (youtrack-post "foo" {:foo :bar}) 
                {:headers {"Content-Type" "application/xml; charset=UTF-8"}
                 :body {:tag :data, :attrs nil, :content ["ok"]}}))
         (is (= (youtrack-post "foo" {:jsessionid {:value "JSESSIONID"}}) 
                {:headers {"Content-Type" "application/xml; charset=UTF-8"}
                 :body {:tag :data, :attrs nil, :content ["ok"]}})))))
  (with-redefs-fn {#'clj-http.client/get (fn [path opts]
                                           {:headers {"Content-Type" "text/csv; charset=UTF-8"}
                                            :body "foo,bar\none,two"})
                   #'clj-http.client/post (fn [path opts]
                                            {:headers {"Content-Type" "text/csv; charset=UTF-8"}
                                             :body "foo,bar\none,two"})}
    #(do
       (testing "Simple youtrack-get"
         (is (= (youtrack-get "foo" {}) 
                {:headers {"Content-Type" "text/csv; charset=UTF-8"}
                 :body [["foo" "bar"] ["one" "two"]]})))
       (testing "Session youtrack-get"
         (is (= (youtrack-get "foo" {:foo :bar}) 
                {:headers {"Content-Type" "text/csv; charset=UTF-8"}
                 :body [["foo" "bar"] ["one" "two"]]}))
         (is (= (youtrack-get "foo" {:jsessionid {:value "JSESSIONID"}}) 
                {:headers {"Content-Type" "text/csv; charset=UTF-8"}
                 :body [["foo" "bar"] ["one" "two"]]})))
       (testing "Simple youtrack-post"
         (is (= (youtrack-post "foo" {}) 
                {:headers {"Content-Type" "text/csv; charset=UTF-8"}
                 :body [["foo" "bar"] ["one" "two"]]})))
       (testing "Session youtrack-post"
         (is (= (youtrack-post "foo" {:foo :bar}) 
                {:headers {"Content-Type" "text/csv; charset=UTF-8"}
                 :body [["foo" "bar"] ["one" "two"]]}))
         (is (= (youtrack-post "foo" {:jsessionid {:value "JSESSIONID"}}) 
                {:headers {"Content-Type" "text/csv; charset=UTF-8"}
                 :body [["foo" "bar"] ["one" "two"]]}))))))

(deftest private-routes 
  (defn guard [_] "whatever")
  (def-private-routes some-private-route guard
    (GET "/foo" [] "bar")
    (GET "/other" [] "newer-hit"))
  (def app (routes  some-private-route))

  (with-redefs-fn {#'guard (fn [_] true)}
    #(testing "Allowed routes"
       (let [response (app (request :get "/foo"))]
         (is (= (:status response) 200))
         (is (= (:body response) "bar")))))
  (with-redefs-fn {#'guard (fn [_] false)}
    #(testing "Deny routes"
       (let [response (app (request :get "/foo"))
             headers (:headers response)]
         (is (= (:status response) 302))
         (is (= (get headers "Location") "/login"))))))

(deftest reports
  (def csv-data
    (shuffle [["TASK-1" "Task1 description" "10" "root"]
              ["TASK-1" "Task1 description" "20" "toor"]
              ["TASK-1" "Task1 description" "30" "user"]
              ["TASK-2" "Task2 description" "10" "root"]
              ["TASK-2" "Task2 description" "20" "toor"]
              ["TASK-2" "Task2 description" "30" "user"]
              ["TASK-3" "Task3 description" "10" "root"]
              ["TASK-3" "Task3 description" "20" "toor"]
              ["TASK-3" "Task3 description" "30" "user"]

              ["TASK-1" "Task1 description" "10" "root"]
              ["TASK-1" "Task1 description" "20" "toor"]
              ["TASK-1" "Task1 description" "30" "user"]
              ["TASK-2" "Task2 description" "10" "root"]
              ["TASK-2" "Task2 description" "20" "toor"]
              ["TASK-2" "Task2 description" "30" "user"]
              ["TASK-3" "Task3 description" "10" "root"]
              ["TASK-3" "Task3 description" "20" "toor"]
              ["TASK-3" "Task3 description" "30" "user"]

              ["TASK-1" "Task1 description" "10" "root"]
              ["TASK-1" "Task1 description" "20" "toor"]
              ["TASK-1" "Task1 description" "30" "user"]
              ["TASK-2" "Task2 description" "10" "root"]
              ["TASK-2" "Task2 description" "20" "toor"]
              ["TASK-2" "Task2 description" "30" "user"]
              ["TASK-3" "Task3 description" "10" "root"]
              ["TASK-3" "Task3 description" "20" "toor"]
              ["TASK-3" "Task3 description" "30" "user"]]))

  (testing "group-report-result"
    (is (= (group-report-result csv-data) {:TASK-1 {:name "Task1 description"
                                                    :participants {:root 30
                                                                   :toor 60
                                                                   :user 90}}

                                           :TASK-2 {:name "Task2 description"
                                                    :participants {:root 30
                                                                   :toor 60
                                                                   :user 90}}

                                           :TASK-3 {:name "Task3 description"
                                                    :participants {:root 30
                                                                   :toor 60
                                                                   :user 90}}})))
  (testing "report-reducer"
    (is (= (report-reducer {} ["TASK-2" "Task2 description" "30" "user"])
           {:TASK-2 {:name "Task2 description", :participants {:user 30}}}))
    (is (= (report-reducer
            {:TASK-2 {:name "Task2 description", :participants {:user 30}}}
            ["TASK-2" "Task2 description" "30" "user"])
           {:TASK-2 {:name "Task2 description", :participants {:user 60}}}))
    (is (= (report-reducer
            {:TASK-2 {:name "Task2 description", :participants {:user 30}}}
            ["TASK-1" "Task1 description" "30" "user"])
           {:TASK-1 {:name "Task1 description", :participants {:user 30}}
            :TASK-2 {:name "Task2 description", :participants {:user 30}}}))
    (is (= (report-reducer
            {:TASK-1 {:name "Task1 description", :participants {:user 30}}}
            ["TASK-1" "Task1 description" "30" "user2"])
           {:TASK-1 {:name "Task1 description", :participants {:user 30 :user2 30}}})))

  (testing "transform-report"
    (is (= (transform-report
            [["Useless" "123" "120"  "0" "root" "Jhon Smith" "TASK-1" "Task1 description" "Jhon Smith (root)" "Dev"]
             ["Useless" "123" "120"  "0" "root" "Jhon Smith" "TASK-1" "Task1 description" "Jhon Smith (root)" "Dev"]])
           [["TASK-1" "Task1 description" "120" "root"]
            ["TASK-1" "Task1 description" "120" "root"]]))))


