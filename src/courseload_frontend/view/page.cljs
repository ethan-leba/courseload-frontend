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
  [:div
   ;; [:h2.text-center (misc-info :course-name)]
   [:div.card-columns
    (cm/cardify-all
     (cm/misc-info-display misc-info class-stats)
     (cm/ranked-teacher-display ranked-professors)
     (cm/hours-per-class-widget (class-stats :hours-per-week)
                                (department-stats :hours-per-week)))
    (cm/generate-horizontal-cards
     [["“I found this course intellectually challenging.”" :challenging]
      ["“The assignments helped me learn.”" :fieldwork]
      ["“The lectures helped me learn.”" :lectures]
      ["“I learned a lot in this course.”" :learning-amount]]
     ["This class" class-stats]
     ["The department average" department-stats])
    (for [response comments]
      (cm/cardify (cm/comment-display response)))]])

(defn no-class-display []
  "Shown when no class has been selected yet"
  [:div.no-class.m-5
   [:h1.display-4.font-weight-bold.text-center "Type in a class on the left to get started."]])

(defn header [data]
  [:div.header
   (when (some? data)
     (when-let [{:keys [course-name subject course-number]} (data :misc-info)]
       [:div.header-text.px-1.py-2
        [:h4.m-0 course-name]
        [:p.header-sub (str subject " " course-number)]]))])

(defn header-text [{:keys [course-name subject course-number]}]
  [:div.header-text.p-2
   [:h4.m-0 course-name]
   [:p.header-sub (str subject " " course-number)]])

(defn search-column [app-state]
  [:div.col.search-col.text-center.shadow-sm.min-vh-100.bg-white
   [:h2.mt-3.mb-4.font-weight-bold "Retrace"]
   [:input.form-control
    {:type "text" :id "fname" :name "fname"
     :placeholder "Search for a class by name or subject..."
     :on-change data/update-search-atom}]
   (for [search-result (app-state :search-response)]
     (let [{:keys [subject course-number]} search-result]
       (cm/class-bar search-result #(data/update-class-atom subject course-number))))])

(defn home-page [data]
  "The landing page of the app"
  [:div.min-vh-100
   [:div.header]
   [:div.container.min-vh-100
    [:div.row.min-vh-100
     (search-column @data/app-state)
     [:div.col
      [:div.row.header-div
       (if-let [class-map (@data/app-state :class-response)]
         (header-text (class-map :misc-info)))]
      [:div.row.mx-2
       (if-let [class-map (@data/app-state :class-response)]
         (class-data-display class-map)
         (no-class-display))]]]]])
