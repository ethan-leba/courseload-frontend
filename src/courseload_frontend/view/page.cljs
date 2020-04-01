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
   [:div.card-columns
    (cm/cardify-all
      (cm/misc-info-display class-stats)
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
  [:div.no-class.my-5
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

(defn term-select []
  [:div.container.mt-2
    [:div.dropdown.col.my-auto
     [:button {:class "btn btn-outline-secondary dropdown-toggle" 
               :type "button" 
               :data-toggle "dropdown"
               :id "dropdownMenuButton"
               :aria-haspopup "true" 
               :aria-expanded "false"} 
      (data/current-term-name)]
     [:div.dropdown-menu {:aria-labelledby "dropdownMenuButton"}
      (for [[term-id term-name] data/terms]
        [:a.dropdown-item {:on-click (fn [] 
                                       (data/update-term-atom term-id)
                                       (data/redirect-class))} term-name])]]])
  
(defn search-bar []
  [:div
    [:input.form-control 
     {:type "text" :id "fname" :name "fname"
      :placeholder "Search for a class by name or subject..."
      :on-change data/update-search-atom}]])

(defn search-column [app-state mobile?]
    [:div.text-center.shadow-sm.min-vh-100.bg-white
     {:class (if mobile? [] ["col" "search-col"])}
     (if mobile?
       [:div.header-mobile.pt-2.text-center.mb-3
        [:h3.mt-2 "Retrace"]]
       [:h2.mt-3.mb-4.font-weight-bold "Retrace"])
     [:div
       {:class (if mobile? ["px-3"] [])}
       (search-bar)
       (term-select)
       (when-let [results (app-state :search-response)]
         (when (= (results :status) "success")
          (for [search-result (results :data)]
            (let [{:keys [subject course-number]} search-result]
              (cm/class-bar search-result (data/get-search-link subject course-number))))))]])

(defn class-error-msg [data]
    [:div.text-center.m-5
      (if (= data "noterm")
          [:div 
           [:h3 "No data exists on this class for the current term."]
           [:h4.text-muted "Try a different term?"]]
          [:div
            [:h3 "Class not found"]])
     [:h6 "If you think this is an error leave an issue " 
      [:a {:href "https://github.com/ethan-leba/courseload-frontend/issues"} "here"]]])

(defn home-page [app-state]
  "The landing page of the app"
  [:div.min-vh-100
   [:div.header]
   [:div.container.min-vh-100
    [:div.row.min-vh-100
     (search-column app-state false)
     [:div.col
       [:div.row.header-div
         (when-let [class-map (app-state :class-response)]
           (when (= (class-map :status) "success")
             (header-text (get-in class-map [:data :misc-info]))))]
       [:div.row.mx-2
         (if-let [class-map (app-state :class-response)]
           (if (= (class-map :status) "success")
             (class-data-display (class-map :data))
             (class-error-msg (class-map :data)))
           (no-class-display))]]]]])

(defn home-page-mobile [app-state]
  (search-column app-state true))

(defn header-mobile [{:keys [course-name subject course-number]}]
  [:div.header-mobile.pt-2.text-center
   [:a {:href "#/"} [:img.search-icon {:src "/icon/search.svg"}]]
   [:h5 course-name]
   [:h6.header-sub (str subject " " course-number)]])

(defn class-page-mobile [app-state]
  (when-let [class-response (app-state :class-response)]
    [:div
      (header-mobile (class-response :misc-info))
      [:div.p-2
        (class-data-display class-response)]]))
