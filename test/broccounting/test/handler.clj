(ns broccounting.test.handler
  (:use clojure.test
        ring.mock.request
        broccounting.handler))

(deftest test-home
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= (:status response) 200))
      (is (re-find #"login" (:body response)))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))
