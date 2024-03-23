(ns clojnito.core
  (:require [clojnito.about]
            [clojnito.cognito :as cognito]
            [clojnito.config :as config]
            [clojnito.home]
            [clojnito.main :as main]
            [clojnito.routes :as routes]
            [day8.re-frame.tracing :refer [fn-traced]]
            [re-frame.core :as re-frame]
            [reagent.dom :as rdom]))

;; --- !! require any namespaces with multimethod definitions !! ---

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [main/main-panel] root-el)))

(defn init []
  (let [env (.-env js/window)
        {:keys [cognito-config]} (js->clj env :keywordize-keys true)]
    (re-frame/dispatch-sync [::initialize-db])
    (routes/start!)
    (cognito/configure cognito-config)
    (cognito/get-auth-data cognito/on-token-refreshed cognito/on-auth-failure)
    (dev-setup)
    (mount-root)))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _] {}))