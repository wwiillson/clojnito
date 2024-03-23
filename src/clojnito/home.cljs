(ns clojnito.home
  (:require [clojnito.routes :as routes]
            [re-frame.core :as re-frame]))

;; ---------- route-dispatchers ----------

(defmethod routes/route-dispatcher :home [cofx route] (routes/default-route-dispatcher cofx route))

;; ---------- subscriptions ----------

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

;; ---------- views ----------

(defn home-panel []
  (let [name (re-frame/subscribe [::name])]
    [:div
     [:h1
      (str "Hello from " @name ". This is the Home Page.")]

     [:div
      [:a {:on-click #(re-frame/dispatch [::routes/navigate (routes/make-route :about)])}
       "go to About Page"]]]))

(defmethod routes/panels :home-panel [] [home-panel])