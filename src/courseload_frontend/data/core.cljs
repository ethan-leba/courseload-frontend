(ns ^:figwheel-always courseload-frontend.data.core
  (:require
    [reagent.core :as r]
    [ajax.core :refer [GET]]))

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

(defn update-search-atom [event]
      (get-data! (str host "search/" (-> event .-target .-value)) :search-response))

(defn update-class-atom [subject course-number]
      (get-data! (str host "class/" subject "?number=" course-number) :class-response))
