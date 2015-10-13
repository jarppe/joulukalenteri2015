(ns frontend.app
  (:require [reagent.ratom :as r]))

(def empty-state
  {})

(defonce app (r/atom empty-state))
