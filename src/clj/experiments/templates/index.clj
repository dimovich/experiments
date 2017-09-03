(ns experiments.templates.index
  (:require [hiccup.page :refer [html5 include-css include-js]]))



(defn index-page []
  (html5
   {:lang "en"}
   [:head
    [:title "Experiments"]]
   [:body
    [:div.wrap
     [:div#app]]
    (include-js "experiments.js")
    [:script "experiments.core.init();"]]))




(defn login-page []
  (html5
   {:lang "en"}
   [:head
    [:title "Experiments"]]
   [:body
    [:form {:method :post}
   
     [:input {:type :text
              :placeholder "Username:"
              :name "username"}]
   
     [:input {:type :password
              :placeholder "Password:"
              :name "password"}]

     [:input {:type :submit
              :value "Submit"}]]]))


