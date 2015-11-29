(set-env!
  :source-paths #{"src/clj" "src/cljs" "src/cljc" "src/less"}
  :test-paths #{"test/clj"}
  :resource-paths #{"src/clj" "src/cljc" "resources"}
  :dependencies '[[adzerk/boot-cljs       "1.7.170-1"  :scope "test"]
                  [adzerk/boot-cljs-repl  "0.2.0"      :scope "test"]
                  [adzerk/boot-reload     "0.4.1"      :scope "test"]
                  [adzerk/boot-test       "1.0.4"      :scope "test"]
                  [deraen/boot-less       "0.4.2"      :scope "test"]
                  [deraen/boot-ctn        "0.1.0"      :scope "test"]

                  [org.clojure/clojure    "1.7.0"]
                  [org.clojure/core.async "0.2.374"]
                  [org.clojure/core.memoize "0.5.8"]

                  ; Backend:
                  [metosin/palikka "0.3.0"]
                  [metosin/maailma "0.2.0"]
                  [metosin/lokit "0.1.0"]
                  [metosin/potpuri "0.2.3"]
                  [metosin/schema-tools "0.7.0"]
                  [prismatic/schema "1.0.3"]
                  [prismatic/plumbing "0.5.2"]

                  ; REST:
                  [metosin/compojure-api "0.24.0"]
                  [ring/ring-core "1.4.0"]
                  [ring/ring-devel "1.4.0"]
                  [ring/ring-defaults "0.1.5"]
                  [metosin/ring-http-response "0.6.5"]
                  [http-kit "2.1.19"]
                  [hiccup "1.0.5"]
                  [enlive "1.1.6"]
                  [ring-webjars "0.1.1"]

                  ; Front:
                  [org.clojure/clojurescript "1.7.170"]
                  [prismatic/dommy "1.1.0"]
                  [alandipert/storage-atom "2.0.0-SNAPSHOT"]

                  ; Assets:
                  [org.webjars.bower/bootstrap "3.3.5" :exclusions [org.webjars.bower/jquery]]
                  [org.webjars.bower/bootswatch "3.3.5" :exclusions [org.webjars.bower/bootstrap]]
                  [org.webjars.bower/font-awesome "4.5.0"]

                  ; Workflow:
                  [reloaded.repl "0.2.1"]
                  [org.clojure/tools.namespace "0.2.11"]
                  [org.clojure/tools.nrepl "0.2.12"]])

(require
  '[adzerk.boot-cljs      :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl repl-env]]
  '[adzerk.boot-reload    :refer [reload]]
  '[adzerk.boot-test      :refer [test]]
  '[deraen.boot-less      :refer [less]]
  '[deraen.boot-ctn       :refer [init-ctn!]]
  '[clojure.java.io       :as io]
  '[backend.main]
  '[reloaded.repl         :refer [go reset start stop system]]
  'user)

(def project-name "joulukalenteri")

(def jar-name (str project-name ".jar"))

; Watch boot temp dirs
(init-ctn!)

(task-options!
  pom {:project (symbol project-name) :version "0.0.2015"}
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
      (reload)
      (less)
      (cljs-repl)
      (cljs :optimizations :none)
      (start-app :port port)))


(deftask copy-jar
  "Copy über-jar under ansible"
  []
  (fn [next-task]
    (fn [fileset]
      (let [result (next-task fileset)]
        (when-let [jar-file (first (by-name [jar-name] (input-files fileset)))]
          (with-open [in (-> jar-file (tmp-file) (io/input-stream))
                      out (-> (str "ansible/" jar-name) (io/file) (io/output-stream))]
            (io/copy in out))
          (println "Copied jar to ansible directory"))
        result))))

(deftask package
  "Build the package"
  []
  (comp
    (less :compression true)
    (cljs :optimizations :advanced)
    (pom)
    (uber)
    (aot)
    (jar :file jar-name)
    (copy-jar)))
