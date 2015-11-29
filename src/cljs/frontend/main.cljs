(ns frontend.main
  (:require [dommy.core :as d :refer-macros [sel sel1]]
            [alandipert.storage-atom :refer [local-storage]]
            [frontend.hatch-pos :refer [hatch-positions]]))

(def debug? true)

(def opened (local-storage (atom {}) :opened))
(def revealed-img "img/rev.1280x905.jpg")

(defn hatch-count []
  (let [now (js/Date.)]
    (cond
      (> (.getYear now) 115) 24
      (< (.getMonth now) 11) 0
      :else (.getDate now))))

(defn hatches []
  (map (fn [{:keys [x y x2 y2]} n can-open?]
         {:x         x
          :y         y
          :w         (- x2 x)
          :h         (- y2 y)
          :n         n
          :opened?   (get @opened n false)
          :can-open? can-open?})
       hatch-positions
       (range 1 25)
       (concat (repeat (if debug? 12 (hatch-count)) true)
               (repeat false))))

(defn open-hatch [{:keys [n x y]} div]
  (let [img (js/document.createElement "img")]
    (doto img
      (d/set-attr! :src revealed-img)
      (d/set-style! :position "relative"
                    :left (str "-" x "px")
                    :top (str "-" y "px")))
    (-> div
        (d/append! img)
        (d/add-class! "opened")
        (d/remove-class! "closed"))
    (swap! opened assoc n true)))

(defn close-hatch [{:keys [n]} div]
  (-> div
      (d/clear!)
      (d/add-class! "closed")
      (d/remove-class! "opened"))
  (swap! opened assoc n false))

(defn on-click-hatch [hatch div e]
  (.preventDefault e)
  (if (d/has-class? div "opened")
    (close-hatch hatch div)
    (open-hatch hatch div)))

(defn mouse-enter [hatch]
  (fn [e]
    (js/console.log "enter:" (:n hatch))))

(defn mouse-leave [hatch]
  (fn [e]
    (js/console.log "leave:" (:n hatch))))

(defn ->hatch-div [{:keys [n x y w h opened? can-open?] :as hatch}]
  (let [div (js/document.createElement "div")]
    (doto div
      (d/add-class! "hatch" (if can-open? "allowed" "forbidden"))
      (d/set-style! :left (str x "px")
                    :top (str y "px")
                    :width (str w "px")
                    :height (str h "px")
                    :line-height (str h "px")))
    (if can-open?
      (d/listen! div :click (partial on-click-hatch hatch div))
      (d/listen! div
                 :mouseenter (mouse-enter hatch)
                 :mouseleave (mouse-leave hatch)))
    (if opened?
      (open-hatch hatch div)
      (close-hatch hatch div))
    (d/append! (sel1 :#image-wrapper) div)))

(defn run []
  (doseq [h (hatches)]
    (->hatch-div h)))

(-> js/window .-onload (set! run))
