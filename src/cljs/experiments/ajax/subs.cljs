(ns experiments.ajax.subs
  (:require [re-frame.core :refer [reg-sub]]))


(reg-sub
 ::token
 (fn [db _]
   (:token db)))

