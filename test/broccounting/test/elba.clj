(ns broccounting.test.elba
 (:use clojure.test
       broccounting.elba))



(deftest bill-utils
  (testing "building bill success"
    (let [bill (build-bill "name" "contractor" [])]
      (is (= (:Number bill) "name"))
      (is (= (:Contractor bill) {:Name "contractor"}))
      (is (= (:Items bill) []))
      (is (re-find 
            #"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$" 
            (:Date bill)))))
  (testing "building bill item success"
    (is (=
          (build-item ["id" "text" "_user" "quantity" "price" "total"])
          {:ProductName "id text"
           :UnitName "час"
           :Quantity "quantity"
           :Price "price"
           :PriceWithoutNds "price"
           :Sum "total"}))))


(deftest bill-api
  (testing "create bill sucess"
    (with-redefs-fn {#'clj-http.client/post 
                     (fn [path opts]
                         (is (= path 
                                "https://elba.kontur.ru/API/CreateBill.ashx"))
                         (is (= (:body opts) "{\"foo\":\"bar\"}"))
                         (is (= (:headers opts) {"X-Login" "login" 
                                                 "X-Password" "password"}))
                         {:body "<data>ok</data>"})}
      #(is (= (create-bill "login" "password" {:foo :bar}) 
              {:body "<data>ok</data>"})))))
