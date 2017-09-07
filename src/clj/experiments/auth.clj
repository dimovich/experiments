(ns experiments.auth
  (:require [buddy.core.nonce  :as nonce]
            [buddy.core.codecs :as codecs]
            [buddy.sign.jwt :as jwt]
            [buddy.hashers  :as hashers]
            [buddy.auth     :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.token :refer [jws-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [clj-time.core      :as time]
            [experiments.util   :refer [ok bad-request]]
            [taoensso.timbre    :as timbre :refer [info]]))



(def secret (-> (nonce/random-bytes 32)
                (codecs/bytes->hex)))


(def authdata
  {:admin "secret"})



(defn login [{{:keys [user pass]} :params :as request}]
  (info "login" request)

  (let [valid? (some-> authdata
                       (get (keyword user))
                       (= pass))]
    (if valid?
      (let [claims {:user (keyword user)
                    :exp  (time/plus (time/now) (time/seconds 3600))}
            token (jwt/sign claims secret {:alg :hs512})]

        (ok {:token token}))
      
      (bad-request {:message "wrong auth data"}))))




(defn unauthorized-handler
  [request metadata]
  (cond
    (authenticated? request)
    (-> (ok)
        (assoc :status 403))

    :else
    (bad-request {:message "unauthorized"})))



(def auth-backend
  (jws-backend {:unauthorized-handler unauthorized-handler
                :secret secret :options {:alg :hs512}}))



;; (hashers/derive pass)
;; (hashers/check pass derived)

