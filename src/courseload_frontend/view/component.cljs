(ns ^:figwheel-always courseload-frontend.view.component
  "Logicless components available for reuse"
  (:require [courseload-frontend.util :as util]))

(defn widget-template [& inside]
  [:div.shadow-sm.card.card-body inside])

(defn big-text-small-text [big small]
  [:div.row.align-items-end
   [:h1.m-0 big]
   [:p.m-1 small]])

(defn vertical-num [title & list-of-vectors]
  "Miscellaneous info about a class"
  [:div.container.px-5
   [:h6.text-center title]
   (for [[big small] list-of-vectors]
     (big-text-small-text big small))])

(defn horizontal-num [title & list-of-vectors]
  [:div.container.text-center
   [:h6 title]
   [:div.row
    (for [[number _] list-of-vectors]
      [:div.col
       [:h1.my-0 number]])]
   [:div.row
    (for [[_ description] list-of-vectors]
      [:div.col
       [:p description]])]])

(def card-attrs ["shadow-sm" "card" "card-body" "px-4"])
(defn cardify [[tag attrs & body]]
  (if (map? attrs)
    [tag (update attrs :class #(conj card-attrs %)) body]
    [tag {:class card-attrs} attrs body]))

(defn cardify-all [& body]
  (map cardify body))

(defn generate-horizontal-cards [lo-cards & data-sources]
  (for [[title attr] lo-cards]
    (cardify
     (apply vertical-num title
            (map (fn [[title data]] [(util/round-float (data attr) 1) title])
                 data-sources)))))

(defn class-bar [{:keys [course-number subject course-name]} click-action]
  "A singular search result"
  ^{:key (str subject course-number)}
  [:div.my-2
   [:a.large-font {:href (str "#/class/" subject "/" course-number)} course-name]
   [:p.font-weight-bold.text-dark (str subject " " course-number)]])

(defn comment-display [{:keys [instructor-name instructor-comments]}]
  "A singular comment"
  (when (some? instructor-comments)
    ^{:key instructor-comments}
    [:div {:class ["my-2"]}
     [:p.my-0 (util/fix-comment-text instructor-comments)]
     [:p.my-0 "on " [:span.font-weight-bold instructor-name]]]))

(defn misc-info-display [{:keys [course-name]} {:keys [responses declines]}]
  "Miscellaneous info about a class"
  (horizontal-num
   "Responses"
   [responses "answered"]
   [declines "declined"]))

(defn ranked-teacher-display [teachers]
  [:div.container.px-5
   [:h6.text-center "Professors by effectiveness"]
   (for [{:keys [instructor-name overall-rating-of-teaching]}
         (reverse (sort-by :overall-rating-of-teaching teachers))]
     ^{:key instructor-name}
     [:div.row.border-10.my-2.py-2
      [:div.col-md-auto
       [:h5.m-0.font-weight-normal instructor-name]]
      [:div.col
       [:h5.m-0.font-weight-normal.text-right
        (util/round-float overall-rating-of-teaching 1)]]])])

(defn hours-per-class-widget [class dept]
  (horizontal-num
   "Hours of work per week"
   [(util/round-float class 1) "Class"]
   [(util/round-float dept 1) "Department"]))

