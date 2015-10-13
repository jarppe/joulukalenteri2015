(ns backend.log
  (:require [clojure.tools.logging :as log])
  (:import [java.util.logging LogManager Logger Level]
            [org.slf4j.bridge SLF4JBridgeHandler]))

(defn init "Activate JUL -> SLF4J bridge" []
  (.reset (LogManager/getLogManager))
  (SLF4JBridgeHandler/install)
  (.setLevel (Logger/getLogger "global") Level/INFO))
