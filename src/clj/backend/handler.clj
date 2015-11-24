(ns backend.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [backend.static :refer [static-routes]]
            [backend.cache :as cache]))

(defapi app
  (swagger-ui "/api-docs")
  (swagger-docs
    :info {:title "API"})

  static-routes)

(defn create-handler [system context]
  (-> #'app
      (cache/wrap-cache {:value    cache/no-cache
                         :default? true})
      (wrap-webjars)))
