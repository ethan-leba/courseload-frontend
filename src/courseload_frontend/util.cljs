(ns ^:figwheel-always courseload-frontend.util
  (:require [clojure.string :as s]))
(defn round-float [n precision]
  "Rounds a float to n decimal places"
  (.toFixed n precision))

;;TODO: Actually have a newline
(defn fix-comment-text [text]
  "Replaces <br/> with a newline."
  (s/replace text #"<br/>" " "))

