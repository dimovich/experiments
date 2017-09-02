(ns experiments.index.subs
  (:require [re-frame.core :refer [reg-sub]]))


#_(reg-sub
   ::index
   (fn [db _]
     (:index db)))

#_(reg-sub
   ::panel-stack
   :<- [::index]
   (fn [db _]
     (:panel-stack db)))
