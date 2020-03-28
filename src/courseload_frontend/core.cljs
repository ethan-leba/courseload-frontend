(ns ^:figwheel-always courseload-frontend.core
  (:require
    [reagent.core :as r]
    [clojure.string :refer [join]]
    [ajax.core :refer [GET]]))

;; -------------------------
;; AJAX
(defonce app-state (r/atom {:search-response []
                            :class-response nil}))

(def host "https://retrace-neu.herokuapp.com/")
;(def host "http://localhost:3000/")

(defn handler [response key-to-update]
  "Takes in an HTTP response and updates the given key with the state"
  (let [clj-map (js->clj (.parse js/JSON response) :keywordize-keys true)]
    (.log js/console (str response))
    (swap! app-state #(assoc % key-to-update (clj-map :data)))))
 

(defn error-handler [{:keys [status status-text]}]
    (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-data! [url key-to-update]
  (.log js/console (str url))
  (GET url {:handler #(handler % key-to-update)
            :error-handler error-handler}))

(defn update-class-atom [subject course-number]
      (get-data! (str host "class/" subject "?number=" course-number) :class-response))

(defn class-bar [{:keys [course-number subject course-name]}]
  ^{:key (str subject course-number)} [:div [:p (str subject " " course-number ": " course-name)]
                                            [:input {:type "button" :value "See more" :on-click #(update-class-atom subject course-number)}]])

(defn denormalize-comm [dict]
    (into {} 
      (map (fn [[k v]] 
             [k (map #(assoc {:text %} :instructor-name (dict :instructor-name)) 
                      v)]) 
           (dissoc dict :instructor-name :_id)))) 

(defn denormalize-comment [comments]
  "Denormalizes into a list of maps"
  (apply merge-with into
         (map denormalize-comm comments)))

;; -------------------------
;; Views
(defn update-search-atom [event]
      (get-data! (str host "search/" (-> event .-target .-value)) :search-response))

(defn comment-display [{:keys [instructor-name instructor-comments]}]
  (when (some? instructor-comments)
   [:p (str instructor-comments " - " instructor-name)]))

(defn misc-info-display [{:keys [course-name]} {:keys [responses declines]}]
    [:div [:h2 course-name]
      [:p (str "responses given: " responses)]
      [:p (str "responses declined: " declines)]])

(defn round-float [n precision]
  (.toFixed n precision))

(defn ranked-teacher-display [teachers]
  (for [{:keys [instructor-name overall-rating-of-teaching]} (reverse (sort-by :overall-rating-of-teaching teachers))]
    ^{:key instructor-name} [:p (str instructor-name ": " (when (not (nil? overall-rating-of-teaching)) 
                                                             (round-float overall-rating-of-teaching 1)))]))

(defn hours-per-class-widget [class dept]
  [:div [:h4 "average hours of work per week"]
        [:p (str "This class: " (round-float class 1))]
        [:p (str "The department avg: " (round-float dept 1))]])
  

(defn class-data-display [{:keys [misc-info comments ranked-professors class-stats department-stats]}]
    [:div (misc-info-display misc-info class-stats)
          [:h4 "teachers ranked by effectiveness"]
          (ranked-teacher-display ranked-professors)
          (hours-per-class-widget (class-stats :hours-per-week) 
                                  (department-stats :hours-per-week))
          [:div.comments
           [:h4 "comments"]
           (for [response comments] 
             (comment-display response))]])

(defn no-class-display []
  [:div.no-class
    [:h2 "Search for a class to start!"]
    [:h4.gray "Astronomy, MATH 1241, CS..."]])

(defn home-page []
  [:div.container
    [:div.fixed [:h2 "Retrace"]
          [:input {:type "text" :id "fname" :name "fname"
                   :placeholder "Search for a class by name or subject..."
                   :on-change update-search-atom}]
          (for [todo (@app-state :search-response)]
            (class-bar todo))]
    [:div.flex-item (if-let [class-map (@app-state :class-response)]
                      (class-data-display class-map)
                      (no-class-display))]])

;; -------------------------
;; Initialize app

(defn router-component []
  "Router component")
  
(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
