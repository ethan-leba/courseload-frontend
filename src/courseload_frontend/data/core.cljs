(ns ^:figwheel-always courseload-frontend.data.core
  (:require
   [reagent.core :as r]
   [ajax.core :refer [GET]]))

;; Responses types - 
(defonce app-state (r/atom {:search-response nil
                            :class-response nil
                            :current-class nil
                            :selected-term nil
                            :page :home}))
(def terms
      {110 "Fall 2019"
       97 "Spring 2019"
       nil "All Terms"})

(defn current-term-name []
  (terms (@app-state :selected-term)))

(defn update-term-atom [term-id]
  (swap! app-state assoc :selected-term term-id))

;(def host "https://retrace-neu.herokuapp.com/")
(def host "http://localhost:3000/")

(defn handler "Takes in an HTTP response and updates the given key with the state"
    [response key-to-update]
  (let [clj-map (js->clj (.parse js/JSON response) :keywordize-keys true)]
    (.log js/console (str response))
    (swap! app-state #(assoc % key-to-update clj-map))))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-data! [url key-to-update]
  (.log js/console (str url))
  (GET url {:handler #(handler % key-to-update)
            :error-handler error-handler}))

(defn get-search-link [subject course-number]
  (str "#/class/" subject "/" course-number
        (when-let [term (@app-state :selected-term)]
          (str "/" term))))
(defn update-search-atom [event]
  (get-data! (str host "search/" (-> event .-target .-value)) :search-response))

(defn clear-class-atom []
  (swap! app-state assoc :class-response nil))

(defn set-page [sym]
  (swap! app-state assoc :page sym))

(defn needs-update [subject course-number term]
  (let [current-class (@app-state :current-class)]
    (not (and (some? current-class)
              (= current-class [subject course-number term])))))

(defn set-current-class! [subject course-number term]
  (swap! app-state assoc :current-class [subject course-number term]))

(defn update-class-atom [subject course-number term]
   (when (needs-update subject course-number term)
     (get-data! (str host 
                     "class/" subject 
                     "?number=" course-number 
                     (when term
                       (str "&term=" term))) 
                :class-response)
     (set-current-class! subject course-number term)))

(defn redirect [link]
  (set! (.. js/document -location -href) link))

(defn redirect-class "Used when updating term" []
  (when-let [current (@app-state :current-class)]
    (redirect (apply get-search-link (take 2 current)))))
