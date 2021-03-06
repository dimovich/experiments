(ns experiments.util
  (:require [reagent.core :as r]))


(enable-console-print!)

(defn info [& args]
  (apply println args))


(defn arc [& args]
  (r/adapt-react-class
   (apply goog.object/getValueByKeys js/window args)))

