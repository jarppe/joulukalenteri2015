(ns backend.static
  (:require [clojure.java.io :as io]
            [compojure.api.sweet :refer :all]
            [compojure.route :refer [resources]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]
            [ring.util.http-response :as resp :refer [ok]]
            [backend.cache :as cache])
  (:import (org.apache.commons.codec.digest DigestUtils)))

(defn with-version
  ([resource-name] (with-version resource-name ""))
  ([resource-name root]
   (str resource-name (some-> (str root resource-name)
                              (io/resource)
                              (io/input-stream)
                              (DigestUtils/sha256Hex)
                              (subs 0 8)
                              (as-> hash (str "?_=" hash))))))

(def index-page
  (html
    (html5
      [:head
       [:title "Millan Joulukalenteri 2015"]
       [:meta {:charset "utf-8"}]
       [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
       [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
       [:link {:rel "icon" :type "image/x-icon" :href (with-version "/favicon.ico" "public")}]
       [:link {:rel "shortcut icon" :type "image/x-icon" :href (with-version "/favicon.ico" "public")}]
       (include-css (with-version "css/main.css"))]
      [:body
       [:div#app
        [:div.loading
         [:div.row
          [:h2.text-center [:i.fa.fa-spinner.fa-spin]]]
         [:div.row
          [:h2.text-center "Odota, Joulukalenteri latautuu..."]]]]
       (include-js (with-version "js/main.js"))])))

(defroutes* static-routes
  (GET* "/" []
    :no-doc true
    (-> (ok index-page)
        (resp/content-type "text/html; charset=\"UTF-8\"")
        (cache/cache-control cache/no-cache)))
  (GET* "/favicon.ico" []
    :no-doc true
    (-> "public/favicon.ico"
        (io/resource)
        (io/input-stream)
        (ok)
        (resp/content-type "image/x-icon")))
  (context* "/js" []
    (resources "" {:root "public/js"})
    (resources "" {:root "js"}))
  (context* "/images" []
    (resources "" {:root "public/images"}))
  (context* "/css" []
    (resources "" {:root "css"})))
