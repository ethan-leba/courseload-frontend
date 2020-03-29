(ns ^:figwheel-always courseload-frontend.view.page
  "Pages and large components consisting of multiple smaller ones, with logic"
  (:require [courseload-frontend.util :refer [round-float]]
            [courseload-frontend.data.core :as data]
            [courseload-frontend.view.component :as cm]))

(defn class-data-display [{:keys [misc-info
                                  comments
                                  ranked-professors
                                  class-stats
                                  department-stats]}]
  "The display for data about a class"
  [:div (cm/misc-info-display misc-info class-stats)
   [:h4 "teachers ranked by effectiveness"]
   (cm/ranked-teacher-display ranked-professors)
   (cm/hours-per-class-widget (class-stats :hours-per-week) 
                              (department-stats :hours-per-week))
   [:div.comments
    [:h4 "comments"]
    (for [response comments] 
      (cm/comment-display response))]])

(defn no-class-display []
  "Shown when no class has been selected yet"
  [:div.no-class
   [:h2 "Search for a class to start!"]
   [:h4.gray "Astronomy, MATH 1241, CS..."]])

(defn home-page []
  "The landing page of the app"
  [:div.container
   [:div.fixed [:h2 "Retrace"]
    [:input {:type "text" :id "fname" :name "fname"
             :placeholder "Search for a class by name or subject..."
             :on-change data/update-search-atom}]
    (for [todo (@data/app-state :search-response)]
      (let [{:keys [subject course-number]} todo]
        (cm/class-bar todo #(data/update-class-atom subject course-number))))]
   [:div.flex-item (if-let [class-map (@data/app-state :class-response)]
                     (class-data-display class-map)
                     (no-class-display))]])
