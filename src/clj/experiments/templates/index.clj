(ns experiments.templates.index
  (:require [hiccup.page :refer [html5 include-css include-js]]))



(defn index []
  (html5
   {:lang "en"}
   [:head
    [:title "Experiments"]]
   [:body
    [:div.wrap
     [:div#app]]
    (include-js "experiments.js")
    [:script "experiments.core.init();"]]))

