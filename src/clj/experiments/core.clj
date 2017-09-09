(ns experiments.core
  (:require [ring.middleware.resource        :refer [wrap-resource]]
            [ring.middleware.content-type    :refer [wrap-content-type]]
            [ring.middleware.not-modified    :refer [wrap-not-modified]]
            [ring.middleware.format          :refer [wrap-restful-format] :as ring-format]
            [ring.util.response              :refer [response file-response redirect not-found content-type]]
            [ring.middleware.session         :refer [wrap-session]]
            [ring.middleware.params          :refer [wrap-params]]
            
            [compojure.core     :refer [defroutes GET POST PUT]]
            [compojure.route    :refer [files resources]]
            [compojure.response :refer [render]]

            [org.httpkit.server :as server]
            [taoensso.timbre    :as timbre :refer [info]]
            [clojure.pprint     :refer [pprint]]
            [clojure.set        :refer [rename-keys]]
            [clojure.data.fressian  :as fress]
            [clojure.java.io    :as io]

            [buddy.sign.jwt :as jwt]
            [buddy.auth     :refer [authenticated? throw-unauthorized]]
            [buddy.auth.middleware :refer [wrap-authentication
                                           wrap-authorization]]
            
            [experiments.db.core]
            [experiments.db.schema]

            [experiments.auth :refer [auth-backend login]]
            [experiments.templates.index :refer [index-page login-page]]

            [experiments.util :refer [ok bad-request]])
  
  (:gen-class))


(defonce state (atom nil))


(defn index [request]
  (if-not (authenticated? request)
    (throw-unauthorized)
    (ok {:message (str "hello " (:identity request))})))



(def covers-sample {:cover1 {:some :idata}
                    :cover2 {:some :odata}})


;; why GET has string-key params? BECAUSE..., you need a special middleware...
;; GET adds its params to the query-string
(defn get-covers [{{:keys [type size skip]} :params :as request}]
  (ok covers-sample))



(defroutes handler
  (GET       "/"       [] (index-page))
  (POST      "/get-covers" [] get-covers)
  (POST      "/login"  [] login)
  (files     "/"       {:root "."})
  (resources "/"       {:root "."})
  (compojure.route/not-found "Page Not Found"))



(defn wrap-info-request [handler]
  (fn [request]
    (info "request: " request)
    (handler request)))


(defn wrap-info-response [handler]
  (fn [request]
    (let [response (handler request)]
      (info "response: " response)
      response)))



(def app
  (as-> handler $
    ;;(wrap-info-request   $)
    (wrap-authorization  $ auth-backend)
    (wrap-authentication $ auth-backend)
    (wrap-restful-format $ {:formats [:transit-json]})
    (wrap-params         $)
    (wrap-resource       $ "public")
    ;;(wrap-info-response  $)
    ))



(defn init []
  #_(db/init))


(defn -main [& args]
  (swap! state assoc :server (server/run-server app {:port 80}))
  (info "started server")
  (init))

