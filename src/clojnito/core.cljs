(ns clojnito.core
  (:require [clojnito.config :as config]
            [clojnito.main :as main]
            [clojnito.routes :as routes]
            [day8.re-frame.tracing :refer [fn-traced]]
            [re-frame.core :as re-frame]
            [reagent.dom :as rdom]

            ;; --- include namespaces with multimethod definitions ---
            [clojnito.home]
            [clojnito.about]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [main/main-panel] root-el)))

(defn init []
  (routes/start!)
  (re-frame/dispatch-sync [::initialize-db])
  (dev-setup)
  (mount-root))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _] {}))