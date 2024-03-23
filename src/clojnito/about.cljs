(ns clojnito.about
  (:require [clojnito.routes :as routes]
            [clojnito.cognito :as cognito]
            [clojnito.views :refer [dispatch-link]]
            [re-frame.core :as re-frame]))

;; ---------- events ----------


;; ---------- subscriptions ----------


;; ---------- views ----------

(defn about-panel []
  (let [auth-data (re-frame/subscribe [::cognito/auth-data])]
    (if @auth-data
      [:div
       [:h1 "About"]

       [:div

        [:div
         [:a {:on-click #(re-frame/dispatch [::routes/navigate (routes/make-route :home)])}
          "go to Home Page"]]

        [:button {:on-click #(cognito/sign-out %)} "Sign Out"]]]

      [:div
       [:h1 "Not authorized"]
       [dispatch-link "Login" {:handler :authenticate}]])))

;; ---------- routes ----------

(defmethod routes/panels :about-panel [] [about-panel])
(defmethod routes/route-dispatcher :about [cofx route] (routes/default-route-dispatcher cofx route))
