(ns experiments.index.events
  (:require [re-frame.core :as rf :refer [reg-event-db path trim-v
                                          dispatch reg-event-fx]]
            [experiments.util :refer [info]]
            [experiments.ajax.events :as ajax-evt]))


(def index-interceptors [(path ::index) trim-v])


(reg-event-db
 ::initialize
 index-interceptors
 (fn [_ _]
   {}))


(reg-event-fx
 ::login
 (fn
   [_ [_ {:keys [user pass]}]]
   {:dispatch [::ajax-evt/request {:method    :post
                                   :params    {:username user
                                               :password pass}
                                   :uri        "/login"
                                   :on-success [::ajax-evt/set-token]}]}))


(reg-event-fx
 ::logout
 (fn
   [{db :db} _]
   {:db (dissoc db :authenticated?)
    :dispatch [::ajax-evt/remove-token]}))



(reg-event-fx
 ::get-home-page
 (fn
   [_ _]
   {:dispatch [::ajax-evt/request-auth {:uri "/home"}]}))

