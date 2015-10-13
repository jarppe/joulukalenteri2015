(ns frontend.main
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [reagent.ratom :refer-macros [reaction]]
            [frontend.app :as app]))

(defn main-view []
  [:div.view.main-view
   [:h1 "Millan Joulukalenteri 2015"]
   [:h2 "Tulossa marraskuussa..."]])

(defn init! []
  (r/render [main-view] (js/document.getElementById "app")))


(init!)
