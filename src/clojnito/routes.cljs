(ns clojnito.routes
  (:require [bidi.bidi :as bidi]
            [clojure.string :as str]
            [day8.re-frame.tracing :refer [fn-traced]]
            [pushy.core :as pushy]
            [re-frame.core :as re-frame]))

(defmulti panels identity)
(defmethod panels :default [] [:div "No panel found for this route."])

(def routes
  (atom
   ["/" {""      :home
         "about" :about}]))

(defn query-params [url]
  (when-let [idx (some-> url (str/index-of "?"))]
    (some->> (subs url idx)
             (js/URLSearchParams.)
             (.entries)
             (map (fn [[k v]] [(keyword k) v]))
             (into {}))))

(defn url->route
  [url]
  (let [params (query-params url)]
    (cond-> (bidi/match-route @routes url)
      params (assoc :query-params params))))

(defn route->url
  [{:keys [handler route-params query-params]}]
  (when-let [url (apply bidi/path-for (concat [@routes handler] (flatten (seq route-params))))]
    (if (seq query-params)
      (str url "?" (-> (js/URLSearchParams. (clj->js query-params)) (.toString)))
      url)))

(defn make-route [handler & [route-params query-params]]
  (cond-> {:handler handler}
    (seq route-params) (assoc :route-params route-params)
    (seq query-params) (assoc :query-params query-params)))

(defn route->panel [route]
  (keyword (str (name (:handler route)) "-panel")))

(defn dispatch-route
  [route]
  (re-frame/dispatch [::dispatch-route route]))

(defonce history
  (pushy/pushy dispatch-route url->route))

(defn navigate!
  [route]
  (pushy/set-token! history (route->url route)))

(defn start!
  []
  (pushy/start! history))

(re-frame/reg-fx
 :navigate
 (fn [handler]
   (navigate! handler)))

;; ---------- events ----------

(re-frame/reg-event-fx
 ::navigate
 (fn-traced
  [_ [_ route]]
  {:navigate route}))

(re-frame/reg-event-fx
 ::dispatch-route
 (fn-traced
  [{:keys [db]} [_ route]]
  (let [active-panel (route->panel route)]
    {:db (assoc db :active-panel active-panel)})))
