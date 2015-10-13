(ns backend.system
  (:require [com.stuartsierra.component :as component :refer [using]]
            [maailma.core :refer [read-config!]]
            [palikka.handler :refer [wrap-env]]
            [palikka.components.http-kit :as http-kit]
            [palikka.components.nrepl :as nrepl]
            [backend.log :as log]
            [backend.handler :refer [create-handler]]))

(defn new-system [override]
  (log/init)
  (let [config (read-config! "backend" override)
        create-handler (fn [system]
                         (-> (create-handler system)
                             (wrap-env (select-keys config [:mode]))))]
    (component/map->SystemMap
      (cond-> {:http    (-> (http-kit/create (:http config) create-handler))}
        (:nrepl config) (assoc :nrepl (nrepl/create (:nrepl config)))))))
