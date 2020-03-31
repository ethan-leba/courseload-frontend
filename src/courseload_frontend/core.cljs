(ns ^:figwheel-always courseload-frontend.core
  "Handles the dirty working of getting the page to display"
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.history.Html5History)
  (:require
   [secretary.core :as secretary]
   [goog.events :as events]
   [goog.history.EventType :as EventType]
   [reagent.core :as r]
   [courseload-frontend.data.core :as data]
   [courseload-frontend.view.page :as page]))

(defonce app-state (r/atom {:search-response []
                            :class-response nil}))

(defn hook-browser-navigation! []
  (doto (Html5History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/set-config! :prefix "#")

  (defroute "/class/:subject/:number" [subject number]
    (data/update-class-atom subject number))

  (defroute "/" []
    (data/clear-class-atom))

  (hook-browser-navigation!))

(defn mount-root []
  "Finds the element with the app id and injects the react component into it"
  (app-routes)
  (r/render [page/home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
