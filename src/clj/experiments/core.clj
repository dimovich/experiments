(ns experiments.core
  (:require [ring.middleware.resource        :refer [wrap-resource]]
            [ring.middleware.content-type    :refer [wrap-content-type]]
            [ring.middleware.not-modified    :refer [wrap-not-modified]]
            [ring.middleware.format          :refer [wrap-restful-format]]
            [experiments.templates.devcards  :refer [devcards]]
            [ring.util.response              :refer [response not-found]]
            [experiments.templates.index     :refer [index]]
            [clojure.data.fressian           :as fress]
            [compojure.core     :refer [defroutes GET POST PUT]]
            [compojure.route    :refer [files resources]]
            [compojure.handler  :refer [site]]
            [ring.util.response :refer [file-response]]
            [org.httpkit.server :as server]
            [taoensso.timbre    :as timbre :refer [info]]
            [clojure.pprint     :refer [pprint]]
            [clojure.set        :refer [rename-keys]]
            
            [experiments.db.core]
            [experiments.db.schema]

            [experiments.util])
  
  (:gen-class))


(defonce state (atom nil))


(defroutes handler
  (GET       "/" [] (index))
  (files     "/" {:root "."})   ;; to serve static resources
  (resources "/" {:root "."})   ;; to serve anything else
  (compojure.route/not-found "Page Not Found")) ;; page not found



(def app
  (-> handler
      (wrap-restful-format)
      (wrap-resource "public")
      (wrap-content-type)
      (wrap-not-modified)
      (site)))



(defn init []
  #_(db/init))



(defn -main [& args]
  (swap! state assoc :server (server/run-server app {:port 80}))
  (info "started server")
  (init))





;; use spec for schema
;; use component to init



#_(rename-keys cover->db-map)
#_(update-in [:cover/marks]
             #(map (fn [m] (rename-keys m mark->db-map)) %))
