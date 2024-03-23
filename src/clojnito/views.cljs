(ns clojnito.views
  (:require [clojnito.routes :as routes]))

(defn dispatch-link [label route]
  [:a {:href (routes/route->url route)} label])
