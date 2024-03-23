(ns clojnito.home
  (:require [clojnito.cognito :as cognito]
            [clojnito.routes :as routes]
            [clojnito.views :refer [dispatch-link]]
            [re-frame.core :as re-frame]))

;; ---------- events ----------


;; ---------- subscriptions ----------


;; ---------- views ----------

(defn home-panel []
  (let [auth-data (re-frame/subscribe [::cognito/auth-data])]
    (if @auth-data
      [:div
       [:h1 "Home"]

       [:div

        [dispatch-link "About" (routes/make-route :about)]

        [:button {:on-click #(cognito/sign-out %)} "Sign Out"]]]

      [:div
       [:h1 "Not authorized"]
       [dispatch-link "Login" {:handler :authenticate}]])))

;; ---------- routes ----------

(defmethod routes/panels :home-panel [] [home-panel])
(defmethod routes/route-dispatcher :home [cofx route] (routes/default-route-dispatcher cofx route))
