(ns clojnito.views
  (:require
   [re-frame.core :as re-frame]
   [clojnito.events :as events]
   [clojnito.routes :as routes]
   [clojnito.subs :as subs]))


;; home

(defn home-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1
      (str "Hello from " @name ". This is the Home Page.")]

     [:div
      [:a {:on-click #(re-frame/dispatch [::routes/navigate (routes/make-route :about)])}
       "go to About Page"]]]))

(defmethod routes/panels :home-panel [] [home-panel])

;; about

(defn about-panel []
  [:div
   [:h1 "This is the About Page."]

   [:div
    [:a {:on-click #(re-frame/dispatch [::routes/navigate (routes/make-route :home)])}
     "go to Home Page"]]])

(defmethod routes/panels :about-panel [] [about-panel])

;; main

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    (routes/panels @active-panel)))
