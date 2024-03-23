(ns clojnito.main
  (:require
   [re-frame.core :as re-frame]
   [clojnito.routes :as routes]))

;; ---------- subscriptions ----------

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

;; ---------- views ----------

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::active-panel])]
    (routes/panels @active-panel)))
