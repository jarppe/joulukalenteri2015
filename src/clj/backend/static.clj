(ns backend.static
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [compojure.api.sweet :refer :all]
            [compojure.route :refer [resources]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]
            [ring.util.http-response :refer [ok]]
            [ring.util.response :as resp]
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
       "ga('create', 'UA-57228234-1', 'auto');"
       "ga('send', 'pageview');"))

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
                  "http://millan-joulukalenteri.fi/img/k.jpeg"

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
       (for [[names content] (partition 2 meta-fields)
             name names]
         [:meta {:name name :content content}])
       (include-css (with-version "css/main.css"))]
      [:body
       [:div#app
        [:h1.waiting "Odota, Millan Joulukalenteri latautuu..."]]
       [:img.preload {:src "img/k.jpeg"}]
       [:img.preload {:src "img/r.jpeg"}]
       [:script google-analytics]
       (include-js "/loc.js")
       (include-js (with-version "js/main.js"))])))

(def default-lang "en")
(def supported-langs #{"fi" "en"})

; Resolve preferred lang from Accept-Language header, like "en-US,en;q=0.8,fi;q=0.6"

(defn get-pref-lang [accept-language]
  (->> (or accept-language default-lang)
       (str/lower-case)
       (re-seq #"([^,]+)(?:,)?")
       (map (comp str/trim second))
       (map (fn [v]
              (let [[_ lang q] (re-matches #"([^;\s]+)\s*(?:;\s*q\s*=\s*(\d+(\.\d*)?))?" v)]
                [(str/replace lang #"\-.*" "") (Double/parseDouble (or q "1.0"))])))
       (filter (fn [[lang :as v]]
                 (if (supported-langs lang) v)))
       (reduce (fn [[_ best-q :as best] [lang q]]
                 (if (> q best-q)
                   [lang q]
                   best))
               [default-lang 0.0])
       (first)))

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
  (GET* "/loc.js" request
    (-> request
        (get-in [:headers "accept-language"])
        (get-pref-lang)
        (as-> lang (format "window.lang = \"%s\";\n" lang))
        (ok)
        (resp/content-type "text/javascript")
        (resp/header "Vary" "Accept-Language")))
  (context* "/js" []
    (resources "" {:root "public/js"})
    (resources "" {:root "js"}))
  (context* "/img" []
    (resources "" {:root "public/img"}))
  (context* "/css" []
    (resources "" {:root "css"})))
