(ns courseload-frontend.prod
  (:require
    [courseload-frontend.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
