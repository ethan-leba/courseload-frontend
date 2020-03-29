(ns ^:figwheel-always courseload-frontend.view.component
  "Logicless components available for reuse"
  (:require [courseload-frontend.util :refer [round-float]]))

(defn class-bar [{:keys [course-number subject course-name]} click-action]
  "A singular search result"
  ^{:key (str subject course-number)} 
  [:div [:p (str subject " " course-number ": " course-name)]
   [:input {:type "button" :value "See more" :on-click click-action}]])

(defn comment-display [{:keys [instructor-name instructor-comments]}]
  "A singular comment"
  (when (some? instructor-comments)
    ^{:key instructor-comments} 
    [:p (str instructor-comments " - " instructor-name)]))

(defn misc-info-display [{:keys [course-name]} {:keys [responses declines]}]
  "Miscellaneous info about a class"
  [:div [:h2 course-name]
   [:p (str "responses given: " responses)]
   [:p (str "responses declined: " declines)]])

(defn ranked-teacher-display [teachers]
  (for [{:keys [instructor-name overall-rating-of-teaching]} 
        (reverse (sort-by :overall-rating-of-teaching teachers))]
    ^{:key instructor-name} 
    [:p (str instructor-name ": " (when (not (nil? overall-rating-of-teaching)) 
                                    (round-float overall-rating-of-teaching 1)))]))

(defn hours-per-class-widget [class dept]
  [:div [:h4 "average hours of work per week"]
        [:p (str "This class: " (round-float class 1))]
        [:p (str "The department avg: " (round-float dept 1))]])
  
