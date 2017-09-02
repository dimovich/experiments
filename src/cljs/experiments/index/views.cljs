(ns experiments.index.views
  (:require [reagent.core  :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))


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



(defn index []
  [:div "hello"])
