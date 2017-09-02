(set-env!
 :source-paths    #{"src/cljs" "src/clj" "src/cljc"}
 :resource-paths  #{"resources" }
 :dependencies '[[org.clojure/clojure "1.9.0-alpha19"]
                 [org.clojure/clojurescript "1.9.908"]

                 [adzerk/boot-cljs-repl     "0.3.3"  :scope "test"]
                 [adzerk/boot-cljs          "2.1.3"  :scope "test"]
                 [adzerk/boot-reload        "0.5.2"  :scope "test"]
                 [pandeiro/boot-http        "0.8.3"  :scope "test"]
                 [com.cemerick/piggieback   "0.2.1"  :scope "test"]
                 [org.clojure/tools.nrepl   "0.2.13" :scope "test"]
                 [weasel                    "0.7.0"  :scope "test"]
                 [tolitius/boot-check       "0.1.4"  :scope "test"]

                 [compojure      "1.6.0"]
                 [ring/ring-core "1.6.2"]
                 [hiccup         "2.0.0-alpha1"]
                 [http-kit       "2.2.0"]
                 [com.taoensso/timbre       "4.8.0"]
                 [javax.servlet/servlet-api "3.0-alpha-1"]

                 
                 [ring-middleware-format "0.7.2"]

                 [buddy/buddy-auth "2.1.0"]
                 [buddy/buddy-hashers "1.3.0"]


                 [com.datomic/clj-client "0.8.606"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/data.fressian "0.2.1"]

                 [prismatic/dommy "1.1.0"]
                 [reagent  "0.8.0-alpha1"]
                 [re-frame "0.10.1"]
                 [cljs-ajax "0.7.2"]])


(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]]
 '[tolitius.boot-check   :as    check])



(swap! boot.repl/*default-dependencies*
       concat '[[cider/cider-nrepl "0.15.1-SNAPSHOT" :scope "provided"]])

(swap! boot.repl/*default-middleware*
       conj 'cider.nrepl/cider-middleware)



(task-options!
 jar   {:main 'experiments.core :file "experiments.jar"}
 sift  {:include #{#"experiments\.jar" #"experiments\.js" #"assets"}}
 aot {:namespace #{'experiments.core}}
 reload {:on-jsload 'experiments.core/reload}
 cljs  { ;;:ids #{"public/experiments"}
        :compiler-options {:output-to  "public/experiments.js"
                           :output-dir "public/out"
                           :asset-path "out"
                           ;;:preloads   '[experiments.dev]
                           :parallel-build true
                           :foreign-libs  [{:file        "src/js/jsutils.js"
                                            :provides    ["jsutils"]
                                            :module-type :commonjs}]}}
 ;;cljs-repl  {:ids #{"public/experiments"}}
 serve {:resource-root "target/public"
        :handler 'experiments.core/app
        :reload true
        :httpkit true})


(deftask production
  []
  (task-options! cljs   {:optimizations :advanced}
                 target {:dir #{"release"}})
  identity)


(deftask development
  []
  (task-options! cljs      {:optimizations :none
                            :source-map    true}
                 cljs-repl {:nrepl-opts {:port 3311}}
                 target    {:dir #{"target"}})
  identity)


(deftask build-jar
  []
  (comp (aot)
        (uber)
        (jar)
        (sift)))


(deftask run []
  (comp
   (serve)
   (watch)
   (reload)
   (cljs-repl)
   (cljs)
   (target)))


(deftask dev
  []
  (task-options! reload {:on-jsload 'experiments.core/reload})
  (comp (development)
        (run)))


(deftask devcards
  []
  (set-env! :source-paths #(conj % "src/devcards"))
  (task-options! reload {:on-jsload 'experiments.devcards/reload}
                 cljs   {:ids #{"public/devcards"}})
  (comp (development)
        (run)))


(deftask prod
  []
  (comp (production)
        (cljs)
        (build-jar)
        (target)))


(deftask check-sources
  []
  (comp
    (check/with-yagni)
    (check/with-eastwood)
    (check/with-kibit)
    (check/with-bikeshed)))
