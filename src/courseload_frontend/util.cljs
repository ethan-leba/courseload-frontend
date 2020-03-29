(ns ^:figwheel-always courseload-frontend.util)

(defn round-float [n precision]
  "Rounds a float to n decimal places"
  (.toFixed n precision))

