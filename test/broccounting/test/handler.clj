(ns broccounting.test.handler
  (:use clojure.test
        ring.mock.request
        broccounting.handler))

(deftest test-home
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= (:status response) 200))
      (is (re-find #"login" (:body response)))))

  (testing "login route"
    (let [response (app (request :get "/login"))
          body (:body response)]
      (is (= (:status response) 200))
      (is (re-find #"<form method=\"POST\">" body))
      (is (re-find #"<input name=\"login\">" body))
      (is (re-find #"<input name=\"password\" type=\"password\">" body))
      (is (re-find #"<input type=\"submit\">" body))))

  (testing "login sucess"
    (with-redefs-fn {#'broccounting.utils/youtrack-post 
                     (fn [& _]
                      {:status 200
                       :body {:content ""}
                       :cookies {"JSESSIONID" {:value "secret-session-cookie"}}})}
      #(let [response (app (request :post "/login" {:login "login"
                                                    :password "password"}))
             headers (:headers response)
             session (:session response)]
         (comment TODO "Add session key check")
         (is (= (:status response) 302))
         (is (= (get headers "Location") "/projects")))))

  (testing "login fail"
    (with-redefs-fn {#'broccounting.utils/youtrack-post 
                     (fn [& _]
                      {:status 401
                       :body {:content ["Some-error-text"]}})}
      #(let [response (app (request :post "/login" {:login "login"
                                                    :password "password"}))
             body (:body response)]
         (is (= (:status response) 200))
         (is (re-find #"Some-error-text" body)))))

  (testing "projects route unauthorized"
    (let [response (app (request :get "/projects"))
          headers (:headers response)]
        (is (= (:status response) 302))
        (is (= (get headers "Location") "/login"))))

  (testing "projects route authorized"
    (with-redefs-fn {#'broccounting.utils/youtrack-get
                     (fn [path session & [opts]]
                         {:status 200 
                          :body {:content []}})}
      #(let [response (app (request :get "/projects"))
             body (:body response)]
         (is (= (:status response) 200))
         (is (re-find #"Projects:" body)))))

  (testing "projects page content"
    (with-redefs-fn {#'broccounting.utils/youtrack-get
                     (fn [path session & [opts]]
                         {:status 200 
                          :body {:content [{:attrs {:id "PROJECT1"}}
                                           {:attrs {:id "PROJECT2"}}
                                           ]}})}
      #(let [response (app (request :get "/projects"))
             body (:body response)]
         (is (= (:status response) 200))
         (is (re-find #"Projects:" body)))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))
