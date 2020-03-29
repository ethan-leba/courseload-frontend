(ns ^:figwheel-always courseload-frontend.core
  (:require
    [reagent.core :as r]
    [courseload-frontend.view.page :as page]))

(defn mount-root []
  (r/render [page/home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
