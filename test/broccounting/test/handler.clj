(ns broccounting.test.handler
 (:use clojure.test
       broccounting.handler)
 (:require [broccounting.youtrack :refer [*youtrack-entrypoint*
                                          *request-cookie-store*]]))


(deftest  test-youtrack-middleware

  (testing "redirect if youtrack not in session "
    (let [handler (youtrack-middleware (fn [request] request))
          response (handler {:session {}})]
      (is (:cs-id (:session response)))
      (is (= (:status response) 302))
      (is (= (:headers response) {"Location" "/login"}))
      (is (= (:body response) ""))))

  (testing "login and css acceptable without youtrack session varaible"
     (let [handler (youtrack-middleware (fn [request] request))
           login-response (handler {:session {}
                                    :uri "/login"})
           style-response (handler {:session {}
                                    :uri "/css/screen.css"})]
       (is (= (dissoc login-response :session)
              {:uri "/login"}))
       (is (= (dissoc style-response :session)
              {:uri "/css/screen.css"}))))

  (testing "all page context have youtrack entrypoint and cookie-storage"
      (let [youtrack-url "http://bro.myjetbrains.com/youtrack/rest"
            handler (youtrack-middleware 
                      (fn [request]
                        (is (= *youtrack-entrypoint* youtrack-url))
                        (is (not (nil? *request-cookie-store*)))
                        request))]
           (handler {:session {:youtrack youtrack-url} :uri "/login"}))))
