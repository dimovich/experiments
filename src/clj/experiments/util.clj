(ns experiments.util
  (:require [taoensso.timbre :as timbre]))


(timbre/set-config!
 {:level :info
  :output-fn (fn [{:keys [timestamp_ level msg_]}]
               (str
                "\n"
                (second (clojure.string/split (force timestamp_) #" ")) " "
                (force msg_)
                "\n"))
  :appenders {:println (timbre/println-appender {:stream :auto})}})
