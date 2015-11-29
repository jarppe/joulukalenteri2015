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

(def google-analytics
  (str "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){"
       "(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),"
       "m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)})"
       "(window,document,'script','//www.google-analytics.com/analytics.js','ga');"
       "ga('create','UA-57228234-1','auto');ga('send','pageview');"))

(def google-fonts
  "family=Merriweather:300italic|Molle:400italic&subset=latin-ext")

(def meta-fields [["title" "og:title" "twitter:title"]
                  "Millan Joulukalenteri 2015"

                  ["description" "og:description" "twitter:description"]
                  "Millan Joulukalenteri vuodelle 2015, avaa uusi luukku joka päivä. Jännää joulun odotusta"

                  ["keywords"]
                  "joulukalenteri,joulu"

                  ["author"]
                  "https://plus.google.com/+JarppeLänsiö"

                  ["copyright"]
                  "Copyrights Milla, Titta ja Jarppe Länsiö 2015"

                  ["application-name"]
                  "Millan Joulukalenteri"

                  ["og:image" "twitter:image"]
                  ""

                  ["og:url" "twitter:url"]
                  "http://millan-joulukalenteri.fi"

                  ["og:type"]
                  "website"

                  ["og:locale"]
                  "fi_FI"

                  ["twitter:card"]
                  "summary"])

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
       [:link {:href (str "https://fonts.googleapis.com/css?" google-fonts) :rel "stylesheet" :type "text/css"}]
       (for [[names content] (partition 2 meta-fields)
             name names]
         [:meta {:name name :content content}])
       (include-css (with-version "css/main.css"))]
      [:body
       [:div#app
        [:h1.waiting "Odota, Millan Joulukalenteri latautuu..."]]
       [:img.preload {:src "img/rev.1280x905.jpg"}]
       (include-js (with-version "js/main.js"))
       [:script google-analytics]])))

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
  (context* "/img" []
    (resources "" {:root "public/img"}))
  (context* "/css" []
    (resources "" {:root "css"})))
