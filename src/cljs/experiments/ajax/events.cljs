(ns experiments.ajax.events
  (:require [re-frame.core :as rf :refer [reg-event-db reg-event-fx subscribe]]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [experiments.util :refer [info]]
            [ajax.core :as ajax :refer [to-interceptor]]
            [experiments.ajax.subs :as sub]))



(defn inject-token [request]
  (if-let [token @(subscribe [::sub/token])]
    (-> request
        (update :headers
                #(merge % {"Authorization" (str "Token " token)})))
    request))

(def token-ajax-interceptor
  (to-interceptor {:name "token interceptor"
                   :request inject-token}))



(reg-event-fx
 ::request
 (fn
   [_ [_ m]]
   {:http-xhrio (-> {:method          :get
                     :on-success      [::good-response]
                     :on-failure      [::bad-response]
                     :format          (ajax/transit-request-format)
                     :response-format (ajax/transit-response-format)}
                    
                    (merge m))}))


(reg-event-fx
 ::request-auth
 (fn
   [_ [_ m]]
   {:dispatch [::request (-> {:interceptors [token-ajax-interceptor]}
                             
                             (merge m))]}))


(reg-event-db
  ::set-token
  (fn
    [db [_ {:keys [token]}]]
    (info "login success: " token)
    (assoc db :token token)))


(reg-event-db
  ::remove-token
  (fn
    [db _]
    (info "logout success.")
    (dissoc db :token)))


(reg-event-db
 ::good-response
 (fn
   [db [_ response]]
   (info "ajax success: " response)
   db))


(reg-event-db
 ::bad-response
 (fn
   [db [_ response]]
   (info "ajax error: " response)
   db))

