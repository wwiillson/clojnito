(ns clojnito.about
  (:require [clojnito.routes :as routes]
            [re-frame.core :as re-frame]))

;; ---------- route-dispatchers ----------

(defmethod routes/route-dispatcher :about [cofx route] (routes/default-route-dispatcher cofx route))

;; ---------- subscriptions ----------


;; ---------- views ----------

(defn about-panel []
  [:div
   [:h1 "This is the About Page."]

   [:div
    [:a {:on-click #(re-frame/dispatch [::routes/navigate (routes/make-route :home)])}
     "go to Home Page"]]])

(defmethod routes/panels :about-panel [] [about-panel])
