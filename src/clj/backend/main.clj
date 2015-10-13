(ns backend.main
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [reloaded.repl :refer [set-init! go]])
  (:gen-class))

(defn init
  ([] (init nil))
  ([opts]
   (require 'backend.system)
   ((resolve 'backend.system/new-system) opts)))

(defn setup-app! [opts]
  (set-init! #(init opts)))

(defn -main [& args]
  (log/info "Application starting...")
  (setup-app! nil)
  (go)
  (log/info "Application running!"))
