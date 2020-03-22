(ns ^:figwheel-always courseload-frontend.core
  (:require
    [reagent.core :as r]
    [clojure.string :refer [join]]
    [ajax.core :refer [GET]]))

;; -------------------------
;; AJAX
(defonce app-state (r/atom {:search-response []
                            :class-response nil}))

(def host "http://retrace-neu.herokuapp.com/")

(defn handler [response key-to-update]
  "Takes in an HTTP response and updates the given key with the state"
  (let [clj-map (js->clj (.parse js/JSON response) :keywordize-keys true)]
    (.log js/console (str response))
    (swap! app-state #(assoc % key-to-update clj-map))))
 

(defn error-handler [{:keys [status status-text]}]
    (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-data! [url key-to-update]
  (.log js/console (str url))
  (GET url {:handler #(handler % key-to-update)
            :error-handler error-handler}))

(defn update-class-atom [id]
      (get-data! (str host "class/" id) :class-response))

(defn class-bar [{:keys [name short-name class-id]}]
  ^{:key class-id} [:div [:p (str short-name ": " name)]
                         [:input {:type "button" :value "See more" :on-click #(update-class-atom class-id)}]])

;; -------------------------
;; Views
(defn update-search-atom [event]
      (get-data! (str host "search/" (-> event .-target .-value)) :search-response))


(defn misc-info-display [{:keys [responses declines class-name class-abbreviation]}]
    [:div [:h2 class-name]
      [:h3 class-abbreviation]
      [:p (str "responses given: " responses)]
      [:p (str "responses declined: " declines)]])

(defn round-float [n precision]
  (.toFixed n precision))

(defn ranked-teacher-display [teachers]
  (for [{:keys [name professor-id effectiveness]} (reverse (sort-by :effectiveness teachers))]
    ^{:key professor-id} [:p (str name ": " (round-float effectiveness 1))]))

(defn hours-per-class-widget [class dept]
  [:div [:h4 "Avg hours of work a week: "]
        [:p (str "This class: " (round-float class 1))]
        [:p (str "The department avg: " (round-float dept 1))]])
  

(defn class-data-display [{:keys [misc-info ranked-professors class-stats department-stats]}]
    [:div (misc-info-display misc-info)
          [:h4 "teachers ranked by effectiveness"]
          (ranked-teacher-display ranked-professors)
          (hours-per-class-widget (class-stats :hours-per-week) 
                                  (department-stats :hours-per-week))])


(defn home-page []
  [:div.container
    [:div.fixed [:h2 "Retrace"]
          [:input {:type "text" :id "fname" :name "fname"
                   :on-change update-search-atom}]
          (for [todo (@app-state :search-response)]
            (class-bar todo))]
    [:div.flex-item (when (@app-state :class-response)
                      (when-let [class-map ((@app-state :class-response) :data)]
                        (class-data-display class-map)))]])

;; -------------------------
;; Initialize app

(defn router-component []
  "Router component")
  
(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
