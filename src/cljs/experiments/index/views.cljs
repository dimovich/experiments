(ns experiments.index.views
  (:require [reagent.core  :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [experiments.index.events :as evt]))


(defn test-fn []
  (let [_ (println "test-fn init")]
    (r/create-class
     {:display-name "test-fn"
      :component-did-mount
      (fn [this]
        (let [_ (println "test-fn mounted")]))
      :reagent-render
      (fn []
        (let [_ (println "test-fn render")]))})))




(defn login-box []
  [:form {:method :post}
   
   [:input {:type :text
            :placeholder "Username:"
            :name "username"}]
   
   [:input {:type :password
            :placeholder "Password:"
            :name "password"}]

   [:input {:type :submit
            :value "Submit"}]])



(defn index []
  (r/with-let [_ (dispatch-sync [::evt/initialize])]
    [:div "hello from index cljs"]
    ;;[login-box]
    ))
