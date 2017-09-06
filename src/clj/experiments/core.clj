(ns experiments.core
  (:require [ring.middleware.resource        :refer [wrap-resource]]
            [ring.middleware.content-type    :refer [wrap-content-type]]
            [ring.middleware.not-modified    :refer [wrap-not-modified]]
            [ring.middleware.format          :refer [wrap-restful-format]]
            [ring.util.response              :refer [response file-response redirect not-found content-type]]
            [ring.middleware.session         :refer [wrap-session]]
            [ring.middleware.params          :refer [wrap-params]]
            
            [compojure.core     :refer [defroutes GET POST PUT]]
            [compojure.route    :refer [files resources]]
            [compojure.response :refer [render]]

            [clj-time.core      :as time]
            [org.httpkit.server :as server]
            [taoensso.timbre    :as timbre :refer [info]]
            [clojure.pprint     :refer [pprint]]
            [clojure.set        :refer [rename-keys]]
            [clojure.data.fressian  :as fress]
            [clojure.java.io    :as io]

            [buddy.core.nonce :as nonce]
            [buddy.sign.jwt :as jwt]
            [buddy.hashers  :as hashers]
            [buddy.auth     :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.token :refer [jws-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            
            [experiments.db.core]
            [experiments.db.schema]
            
            [experiments.util]

            [experiments.templates.index :refer [index-page login-page]])
  
  (:gen-class))


(defonce state (atom nil))

(defn ok [d]          {:status 200 :body d})
(defn bad-request [d] {:status 400 :body d})


(def secret "mysupersecret")


(defn index [request]
  (info "index" request)
  
  (if-not (authenticated? request)
    (throw-unauthorized)
    (ok {:message (str "hello" (:identity request))})))


(def authdata
  {:admin "secret"
   :user  "secret"})




(defn login [request]
  (info "login" request)
  (let [username (get-in request [:params :username])
        password (get-in request [:params :password])
        valid?   (some-> authdata
                         (get (keyword username))
                         (= password))]

    (if valid?
      (let [claims {:user (keyword username)
                    :exp  (time/plus (time/now) (time/seconds 3600))}
            token (jwt/sign claims secret {:alg :hs512})]
        (ok {:token token}))
      
      (bad-request {:message "wrong auth data"}))))





(defroutes handler
  (GET       "/"       [] (index-page))
  (GET       "/home"   [] index)
  (POST      "/login"  [] login)
  (files     "/"       {:root "."})   ;; to serve static resources
  (resources "/"       {:root "."})   ;; to serve anything else
  (compojure.route/not-found "Page Not Found")) ;; page not found




(def auth-backend
  (jws-backend {:secret secret :options {:alg :hs512}}))




(def app
  (as-> handler $
    (wrap-authorization  $ auth-backend)
    (wrap-authentication $ auth-backend)
    ;;    (wrap-params $)
    ;;    (wrap-session $)
    (wrap-restful-format $ {:formats [:transit-json]}
                         )
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

