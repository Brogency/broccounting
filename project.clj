(defproject broccounting "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.5"]
                 [ring-server "0.3.1"]
                 [clj-time "0.9.0"]
                 [cheshire "5.4.0"]
                 [clj-http "1.0.1"]
                 [cljfmt "0.1.7"]
                 [jonase/eastwood "0.2.1"]
                 [org.clojure/data.csv "0.1.2"]]
  :plugins [[lein-ring "0.8.12"]
            [lein-cljfmt "0.1.7"]
            [jonase/eastwood "0.2.1"]
            [lein-cloverage "1.0.2"]]
  :ring {:handler broccounting.handler/app
         :init broccounting.handler/init
         :destroy broccounting.handler/destroy}
  :profiles
  {:uberjar {:aot :all}
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring-mock "0.1.5"] 
                   [ring/ring-devel "1.3.1"]]}})
