(ns experiments.index.views
  (:require [reagent.core  :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [ajax.core :refer [GET POST default-interceptors to-interceptor]]
            [experiments.index.events :as evt]))



(def token (atom nil))

(defn inject-token [request]
  (if @token
    (-> request
        (update :headers
                #(merge % {"Authorization" (str "Token " @token)})))
    request))


(def token-interceptor
     (to-interceptor {:name "token interceptor"
                      :request inject-token}))



(defn login-handler [user pass]
  (POST "/login"
        {:params {:username user
                  :password pass}
         :headers {"Accept" "application/transit+json"}
         :handler #(do (println %)
                       (reset! token (:token %)))
         :error-handler #(println %)}))


(defn get-home []
  (GET "/home"
       {:headers {;;"Authorization" (str "Token " @token)
                  "Accept" "application/transit+json"}
        :handler #(println %)
        :interceptors [token-interceptor]
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
