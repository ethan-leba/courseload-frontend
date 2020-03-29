(ns ^:figwheel-always courseload-frontend.core
  "Handles the dirty working of getting the page to display"
  (:require
    [reagent.core :as r]
    [courseload-frontend.view.page :as page]))

(defn mount-root []
  "Finds the element with the app id and injects the react component into it"
  (r/render [page/home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
