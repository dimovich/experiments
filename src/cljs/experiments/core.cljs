(ns experiments.core
  (:require [reagent.core :as r]
            [dommy.core   :refer-macros [sel1]]
            [experiments.index.views  :as index]))


(defn app []
  [index/index])


(defn ^:export reload []
  (r/render [app] (sel1 :#app)))


(defn ^:export init []
  (reload))


