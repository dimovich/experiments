(require '[cljs.build.api :as b])

(b/build "src" {:output-dir "out"
                :asset-path "/out"
                :output-to "out/main.js"
                :main 'foo.core
                :npm-deps {:react "15.6.1" :react-dom "15.6.1"}
                :install-deps true
                :optimizations :advanced
;;                :verbose true
                })
