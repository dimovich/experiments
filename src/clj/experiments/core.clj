(ns experiments.core
  (:require [ring.middleware.resource        :refer [wrap-resource]]
            [ring.middleware.content-type    :refer [wrap-content-type]]
            [ring.middleware.not-modified    :refer [wrap-not-modified]]
            [ring.middleware.format          :refer [wrap-restful-format]]
            [ring.util.response              :refer [response file-response redirect not-found content-type]]
            
            [compojure.core     :refer [defroutes GET POST PUT]]
            [compojure.route    :refer [files resources]]
            [compojure.handler  :refer [site]]
            [compojure.response :refer [render]]
            
            [org.httpkit.server :as server]
            [taoensso.timbre    :as timbre :refer [info]]
            [clojure.pprint     :refer [pprint]]
            [clojure.set        :refer [rename-keys]]
            [clojure.data.fressian           :as fress]

            [clojure.java.io    :as io]

            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.params  :refer [wrap-params]]

            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            
            [experiments.db.core]
            [experiments.db.schema]
            
            [experiments.util]

            [experiments.templates.index :refer [index-page login-page]])
  
  (:gen-class))


(defonce state (atom nil))


(defn index [request]
  (if-not (authenticated? request)
    (throw-unauthorized)
    (render (index-page) request)))


(defn login [request]
  (render (login-page) request))


(defn logout [request]
  (-> (redirect "/login")
      (assoc :session {})))


(def authdata
  {:admin "secret"
   :user  "secret"})




(defn login-authenticate [request]
  (let [username (get-in request [:form-params "username"])
        password (get-in request [:form-params "password"])
        session (:session request)
        found-password (get authdata (keyword username))]
    (if (and found-password (= found-password password))
      (let [next-url (get-in request [:query-params :next] "/")
            updated-session (assoc session :identity (keyword username))]
        (-> (redirect next-url)
            (assoc :session updated-session)))
      (render (login-page) request))))




(defroutes handler
  (GET       "/"       [] index)
  (GET       "/login"  [] login)
  (POST      "/login"  [] login-authenticate)
  (GET       "/logout" [] logout)
  (files     "/" {:root "."})   ;; to serve static resources
  (resources "/" {:root "."})   ;; to serve anything else
  (compojure.route/not-found "Page Not Found")) ;; page not found




(defn unauthorized-handler [request metadata]
  (let [current-url (:uri request)]
    (redirect (format "/login?next=%s" current-url)))
  #_(cond
      (authenticated? request) (-> (render "Authorization Error" request)
                                   (assoc :status 403))
    
      :else ))



(def auth-backend
  (session-backend {:unauthorized-handler unauthorized-handler}))




(def app
  (as-> handler $
    (wrap-authorization  $ auth-backend)
    (wrap-authentication $ auth-backend)
    (wrap-restful-format $)
    (wrap-params $)
    (wrap-session $)
    (wrap-resource       $ "public")
    ;;(wrap-content-type   $)
    ;;(wrap-not-modified   $)
    (site $)))



(defn init []
  #_(db/init))



(defn -main [& args]
  (swap! state assoc :server (server/run-server app {:port 80}))
  (info "started server")
  (init))

