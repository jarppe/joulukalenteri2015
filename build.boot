(set-env!
  :source-paths #{"src/clj" "src/cljs" "src/cljc" "src/less"}
  :test-paths #{"test/clj"}
  :resource-paths #{"src/clj" "src/cljc" "resources"}
  :dependencies '[[adzerk/boot-cljs       "1.7.48-5"   :scope "test"]
                  [adzerk/boot-cljs-repl  "0.2.0"      :scope "test"]
                  [adzerk/boot-reload     "0.4.0"      :scope "test"]
                  [adzerk/boot-test       "1.0.4"      :scope "test"]
                  [deraen/boot-less       "0.4.2"      :scope "test"]
                  [deraen/boot-ctn        "0.1.0"      :scope "test"]

                  [org.clojure/clojure      "1.7.0"]
                  [org.clojure/core.async   "0.1.346.0-17112a-alpha"]
                  [org.clojure/core.memoize "0.5.6"]

                  ; Backend:
                  [metosin/palikka "0.2.0-SNAPSHOT"]

                  ; REST:
                  [ring/ring-core "1.4.0"]
                  [ring/ring-devel "1.4.0"]
                  [ring/ring-defaults "0.1.5"]
                  [metosin/compojure-api "0.24.0-SNAPSHOT"]
                  [metosin/ring-http-response "0.6.5"]
                  [http-kit "2.1.19"]
                  [hiccup "1.0.5"]
                  [enlive "1.1.6"]
                  [ring-webjars "0.1.1"]

                  ; Front:
                  [org.clojure/clojurescript "1.7.107"]
                  [com.domkm/silk "0.1.1"]
                  [reagent "0.5.1"]

                  ; Assets:
                  [org.webjars.bower/bootstrap "3.3.5" :exclusions [org.webjars.bower/jquery]]
                  [org.webjars.bower/bootswatch "3.3.5" :exclusions [org.webjars.bower/bootstrap]]
                  [org.webjars.bower/font-awesome "4.4.0"]

                  ; Workflow:
                  [reloaded.repl "0.2.0"]
                  [org.clojure/tools.namespace "0.2.11"]
                  [org.clojure/tools.nrepl "0.2.11"]

                  ; Logging:
                  [org.clojure/tools.logging "0.3.1"]
                  [ch.qos.logback/logback-classic "1.1.3"]
                  [org.slf4j/slf4j-api "1.7.12"]
                  [org.slf4j/jul-to-slf4j "1.7.12"]       ; JUL to SLF4J
                  [org.slf4j/jcl-over-slf4j "1.7.12"]     ; JCL to SLF4J
                  [org.slf4j/log4j-over-slf4j "1.7.12"]   ; Log4j to SLF4J
                  ])

(require
  '[adzerk.boot-cljs      :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl repl-env]]
  '[adzerk.boot-reload    :refer [reload]]
  '[adzerk.boot-test      :refer [test]]
  '[deraen.boot-less      :refer [less]]
  '[deraen.boot-ctn       :refer [init-ctn!]]
  '[backend.main]
  '[reloaded.repl         :refer [go reset start stop system]]
  'user)

; Watch boot temp dirs
(init-ctn!)

(deftask start-app
  [p port   PORT int  "Port"]
  (let [x (atom nil)]
    (with-post-wrap fileset
      (swap! x (fn [x]
                 (if x
                   x
                   (do (backend.main/setup-app! {:port port})
                       (go)))))
      fileset)))

(task-options!
  pom {:project 'joulukalenteri :version "0.0.2015"}
  aot {:namespace #{'backend.main 'com.stuartsierra.component 'com.stuartsierra.dependency}}
  jar {:main 'backend.main}
  cljs {:source-map true}
  less {:source-map true})

(deftask start-app
  [p port   PORT int  "Port"]
  (let [x (atom nil)]
    (with-post-wrap fileset
      (swap! x (fn [x]
                 (if x
                   x
                   (do (backend.main/setup-app! {:port port})
                       (go)))))
      fileset)))

(deftask dev
  "Start the dev env..."
  [p port       PORT int  "Port for web server"]
  (set-env! :source-paths #(conj % "test/clj"))
  (comp
    (watch)
    (reload :on-jsload 'frontend.main/init!)
    (less)
    (cljs-repl)
    (cljs :optimizations :none :compiler-options {:asset-path "/js/main.out"})
    (start-app :port port)))


(deftask package
  "Build the package"
  []
  (comp
    (less :compression true)
    (cljs :optimizations :advanced)
    (pom)
    (uber)
    (aot)
    (jar :file "joulukalenteri.jar")))

(deftask run-tests []
  (set-env! :source-paths #(conj % "test/clj"))
  (comp
    ; FIXME: Only test -test namespaces
    (test)))
