(ns broccounting.test.utils
  (:use clojure.test
        ring.mock.request
        broccounting.utils)
  (:require
        [compojure.core :refer [GET routes]]))


(deftest youtrack-api
  (with-redefs-fn {#'clj-http.client/get (fn [path opts] {:body "<data>ok</data>"})
                   #'clj-http.client/post (fn [path opts] {:body "<data>ok</data>"})}
    #(do
        (testing "Simple youtrack-get"
          (is (=(youtrack-get "foo" {}) 
                {:body {:tag :data, :attrs nil, :content ["ok"]}})))
        (testing "Session youtrack-get"
          (is (= (youtrack-get "foo" {:foo :bar}) 
                 {:body {:tag :data, :attrs nil, :content ["ok"]}}))
          (is (= (youtrack-get "foo" {:jsessionid {:value "JSESSIONID"}}) 
                 {:body {:tag :data, :attrs nil, :content ["ok"]}})))
        (testing "Simple youtrack-post"
          (is (= (youtrack-post "foo" {}) 
                 {:body {:tag :data, :attrs nil, :content ["ok"]}})))
        (testing "Session youtrack-post"
          (is (= (youtrack-post "foo" {:foo :bar}) 
                 {:body {:tag :data, :attrs nil, :content ["ok"]}}))
          (is (= (youtrack-post "foo" {:jsessionid {:value "JSESSIONID"}}) 
                 {:body {:tag :data, :attrs nil, :content ["ok"]}}))))))

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
            headers(:headers response)]
        (is (= (:status response) 302))
        (is (= (get headers "Location")"/login"))))))
