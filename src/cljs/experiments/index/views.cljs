(ns experiments.index.views
  (:require [reagent.core  :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [ajax.core :refer [GET POST]]
            [experiments.index.events :as evt]))




(defn login-handler [user pass]
  (POST "/login"
        {:params {:user user
                  :pass pass}
         ;;:with-credentials true
         :handler #(println %)
         :error-handler #(println %)}))


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
                :on-click #(login-handler (:user @state)
                                          (:pass @state))}]])))


(defn index []
  (r/with-let [_ (dispatch-sync [::evt/initialize])]
    [:div
     [:h1 "hello from index cljs"]
     [login-form]]))
