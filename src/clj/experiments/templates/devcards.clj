(ns experiments.templates.devcards
  (:require [hiccup.page :refer [html5 include-css include-js]]))


(defn devcards []
  (html5
   {:lang "en"}
   [:head
    [:title "Coverton Editor - Devcards"]
    (include-css "assets/css/style.css")]
   [:body
    [:div.wrap
     [:div#app]]
    (include-js "main.js")
    [:script "experiments.devcards.init();"]]))


