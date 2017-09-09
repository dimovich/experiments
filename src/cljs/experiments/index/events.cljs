(ns experiments.index.events
  (:require [re-frame.core :as rf :refer [reg-event-db path trim-v reg-event-fx]]
            [experiments.util     :refer [info]]
            [experiments.ajax.events :as ajax-evt]))


(def index-interceptors [(path :index) trim-v])


(reg-event-db
 ::initialize
 index-interceptors
 (fn [_ _]
   {}))


(reg-event-fx
 ::login
 index-interceptors
 (fn [_ [creds]]
   {:dispatch
    [::ajax-evt/request {:method     :post
                         :uri        "/login"
                         :params     creds
                         :on-success [::ajax-evt/set-token]}]}))



(reg-event-fx
 ::logout
 index-interceptors
 (fn [{db :db} _]
   {:db (dissoc db :authenticated?)
    :dispatch [::ajax-evt/remove-token]}))



;;editor
(reg-event-fx
 ::save-cover
 (fn [_ [_ cover]]
   {:dispatch
    [::ajax-evt/request-auth {:method :post
                              :uri "/save-cover"
                              :params cover}]}))



;;index
(reg-event-fx
 ::get-covers
 index-interceptors
 (fn [_ [opts]]
   {:dispatch
    [::ajax-evt/request-auth {:method :post
                              :uri "/get-covers"
                              :params opts
                              :on-success [::import-covers]}]}))



(reg-event-db
 ::import-covers
 index-interceptors
 (fn [db [covers]]
   (assoc db :covers covers)))
