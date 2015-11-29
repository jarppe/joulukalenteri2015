(ns frontend.main
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [<!] :as a]
            [reagent.core :as r]
            [alandipert.storage-atom :refer [local-storage]]
            [frontend.hatch-pos :refer [hatch-positions]]))

(def debug? true)

(def revealed-img "img/rev.1280x905.jpg")
(defn px [v] (str v "px"))

(def active-hatches
  (if debug?
    12
    (let [now (js/Date.)]
      (cond
        (> (.getYear now) 115) 24
        (< (.getMonth now) 11) 0
        :else (.getDate now)))))

(defn make-hatches []
  (map (fn [{:keys [x y x2 y2]} n can-open?]
         {:n         n
          :x         x
          :y         y
          :w         (- x2 x)
          :h         (- y2 y)
          :can-open? can-open?})
       hatch-positions
       (range 1 25)
       (concat (repeat active-hatches true)
               (repeat false))))

(def numbers-in-fi {3  "kolme"
                    4  "neljä"
                    5  "viisi"
                    6  "kuusi"
                    7  "seitsemän"
                    8  "kahdeksan"
                    9  "yhdeksän"
                    10 "kymmenen"
                    11 "yksitoista"
                    12 "kaksitoista"})

(defn make-tooltip-message [n]
  (let [days-until (- n active-hatches)]
    (case days-until
      1 "Tämän luukun voit avata jo huomenna"
      2 "Enää kaksi yötä niin saat avata tämän luukun"
      (str "Vielä " (numbers-in-fi days-until (str days-until)) " yötä tähän luukkuun"))))

(defn active-hatch-component [{:keys [n x y w h]} opened?]
  [:div.hatch.allowed {:class    (if @opened? "opened" "closed")
                       :style    {:left        (px x)
                                  :top         (px y)
                                  :width       (px w)
                                  :height      (px h)
                                  :line-height (px h)}
                       :on-click (fn [_] (js/console.log "open" n) (swap! opened? not) nil)}
   (if @opened?
     [:img {:style {:position "relative"
                    :left     (px (* x -1))
                    :top      (px (* y -1))}
            :src   revealed-img}])])

(defn inactive-hatch-component [{:keys [n x y w h]}]
  (let [tooltip-message (make-tooltip-message n)
        tooltip? (r/atom false)
        tooltip! (fn [v] (reset! tooltip? v))
        tooltip-task (atom nil)
        mouse-enter (fn [_]
                      (js/clearTimeout @tooltip-task)
                      (reset! tooltip-task (js/setTimeout tooltip! 500 true))
                      nil)
        mouse-leave (fn [_]
                      (js/clearTimeout @tooltip-task)
                      (reset! tooltip-task (js/setTimeout tooltip! 100 false))
                      nil)]
    (fn []
      [:div
       [:div.hatch {:class          "closed forbidden"
                    :style          {:left        (px x)
                                     :top         (px y)
                                     :width       (px w)
                                     :height      (px h)
                                     :line-height (px h)}
                    :on-mouse-enter mouse-enter
                    :on-mouse-leave mouse-leave}]
       (if @tooltip?
         [:div.tooltip {:style {:left (px x)
                                :top  (px (+ y h 10))}}
          [:p tooltip-message]])])))

(defn hatch-component [{:keys [can-open?] :as hatch} opened?]
  (if can-open?
    [active-hatch-component hatch opened?]
    [inactive-hatch-component hatch]))

(defn main-view []
  (let [opened (local-storage (r/atom {}) :opened-2015)]
    (fn []
      [:div
       [:header
        [:h1 "Millan Joulukalenteri 2015"]
        [:h2 "Etsi luukkuja hiirellä. Voit avata tämän ja edellisten päivien luukkut hiiren painalluksella."]]
       [:article
        [:div#image-wrapper
         (for [{n :n :as hatch} (make-hatches)]
           ^{:key n} [hatch-component hatch (r/cursor opened [n])])]
        [:img#main-image {:src "img/cal.1280x905.jpg"}]]
       [:footer
        [:p "Taide Copyrights \u00A9 2015 Milla Länsiö and Titta Länsiö"]
        [:p "Koodi Copyrights \u00A9 2015 Jarppe Länsiö"]]])))

(defn init! []
  (r/render [main-view] (js/document.getElementById "app")))

(init!)
