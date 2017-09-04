(ns experiments.core
  (:require [ring.middleware.resource        :refer [wrap-resource]]
            [ring.middleware.content-type    :refer [wrap-content-type]]
            [ring.middleware.not-modified    :refer [wrap-not-modified]]
            [ring.middleware.format          :refer [wrap-restful-format]]
            [ring.util.response              :refer [response file-response redirect not-found content-type]]
            
            [compojure.core     :refer [defroutes GET POST PUT]]
            [compojure.route    :refer [files resources]]
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

(defn ok [d]          {:status 200 :body d})
(defn bad-request [d] {:status 400 :body d})


(defn index [request]
  (info request)
  (render (index-page) request)
  #_(if-not (authenticated? request)
      (throw-unauthorized)
      ))


(defn login [request]
  (render (login-page) request))


(defn logout [request]
  (-> (ok {:status :logged-out}) ;;(redirect "/login")
      (assoc :session {})))


(def authdata
  {:admin "sec"
   :user  "sec"})


(defn login-authenticate [request]
  (info request)
  (let [username (get-in request [:params :user])
        password (get-in request [:params :pass])
        session (:session request)
        found-password (get authdata (keyword username))]
    
    (if (and found-password (= found-password password))
      
      (let [next-url (get-in request [:query-params :next] "/")
            updated-session (assoc session :identity (keyword username))]
        
        (-> (ok {:user username})
            (assoc :session updated-session)))
      
      (render (login-page) request))))


(defn save-cover [request]
  (if (authenticated? request)
    (ok {:saved :cover})
    (bad-request {:could :not})))


(defroutes handler
  (GET       "/"       [] index)
  (GET       "/login"  [] login)
  (POST      "/login"  [] login-authenticate)
  (POST      "/save-cover" [] save-cover)
  (GET       "/logout" [] logout)
  (files     "/" {:root "."})   ;; to serve static resources
  (resources "/" {:root "."})   ;; to serve anything else
  (compojure.route/not-found "Page Not Found")) ;; page not found







(defn unauthorized-handler [request metadata]
  (let [current-url (:uri request)]
    (cond
      (authenticated? request)
      (-> (render "Authorization Error" request)
          (assoc :status 403))
      
      :else (redirect (format "/login?next=%s" current-url)))))



(def auth-backend
  (session-backend {:unauthorized-handler unauthorized-handler}))




(def app
  (as-> handler $
    (wrap-authorization  $ auth-backend)
    (wrap-authentication $ auth-backend)
    (wrap-params $)
    (wrap-session $)
    (wrap-restful-format $)
    (wrap-resource       $ "public")
    ;; (wrap-content-type   $)
    ;; (wrap-not-modified   $)
    ))



(defn init []
  #_(db/init))



(defn -main [& args]
  (swap! state assoc :server (server/run-server app {:port 80}))
  (info "started server")
  (init))

