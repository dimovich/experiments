(ns experiments.index.subs
  (:require [re-frame.core :refer [reg-sub]]
            [experiments.ajax.subs :as ajax-sub]))


(reg-sub
 ::authenticated?
 :<- [::ajax-sub/token]
 (fn [token _]
   (not (nil? token))))


