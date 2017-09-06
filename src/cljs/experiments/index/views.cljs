(ns experiments.index.views
  (:require [reagent.core  :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [ajax.core :as ajax :refer [GET POST]]
            [experiments.index.events :as evt]
            [experiments.index.subs :as sub]))



(defn login-form []
  (let [state (r/atom {:user "" :pass ""})] 
    (fn [] 
      [:form
       [:input {:type :text
                :placeholder "Username:"
                :value (:user @state)
                :on-change #(swap! state assoc :user (.. % -target -value))}]
       [:input {:type :password
                :placeholder "Password:"
                :value (:pass @state)
                :on-change #(swap! state assoc :pass (.. % -target -value))}]
       [:input {:type "button"
                :value "Submit"
                :on-click #(dispatch [::evt/login @state])}]])))


(defn index []
  (r/with-let [_ (dispatch-sync [::evt/initialize])]
    [:div
     [:h1 "hello from index cljs"]
     
     [:input {:type :button
              :value "Get Page"
              :on-click #(dispatch [::evt/get-home-page])}]
     
     (if @(subscribe [::sub/authenticated?])
       [:input {:type :button
                :value "Logout"
                :on-click #(dispatch [::evt/logout])}]
       [login-form])]))
