(ns experiments.index.events
  (:require [re-frame.core :as rf :refer [reg-event-db path trim-v dispatch]]))



(def index-interceptors [(path :index) trim-v])

(reg-event-db
 ::initialize
 index-interceptors
 (fn [_ _]
   {}))


#_(reg-event-db
   ::update
   index-interceptors
   (fn [db [ks v]]
     (assoc-in db ks v)))
