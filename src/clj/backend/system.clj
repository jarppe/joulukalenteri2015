(ns backend.system
  (:require [com.stuartsierra.component :as component :refer [using]]
            [maailma.core :as m]
            [palikka.core :refer [providing]]
            [palikka.handler :refer [wrap-env wrap-context]]
            [palikka.components.http-kit :as http-kit]
            [palikka.components.nrepl :as nrepl]
            [backend.handler :refer [create-handler]]))

(defn new-system [override]
  (let [config (m/build-config
                 (m/resource "config-defaults.edn")
                 (m/file "./config-local.edn")
                 override)
        create-handler (partial create-handler config)]
    (component/map->SystemMap
      (cond-> {:http    (-> (http-kit/create (:http config) {:fn create-handler}))}
        (:nrepl config) (assoc :nrepl (nrepl/create (:nrepl config)))))))

