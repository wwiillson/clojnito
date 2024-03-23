(ns clojnito.cognito
  (:require ["aws-amplify" :as amp :refer [Amplify]]
            ["aws-amplify/auth" :as amp-auth :refer [fetchAuthSession
                                                     signInWithRedirect
                                                     signOut]]
            ["aws-amplify/utils" :as amp-utils :refer [Hub I18n]]
            [clojnito.routes :as routes]
            [clojure.string :as str]
            [day8.re-frame.tracing :refer [fn-traced]]
            [re-frame.core :as re-frame]))

(defn extract-auth-data [session]
  (when session
    (let [id-token (some-> session (.-tokens) (.-idToken))
          payload  (some-> id-token (.-payload) (js->clj :keywordize-keys true))]
      (when payload
        {:token       (.toString id-token)
         :permissions (-> payload
                          :permissions
                          (str/split #",")
                          (->> (map str/trim)
                               set))}))))

(defn get-auth-data [ok err]
  (-> (fetchAuthSession)
      (.then ok)
      (.catch err)))

(defn- js->event [^js hub-event]
  (-> hub-event
      .-payload
      .-event))

(defn on-token-refreshed [session]
  (when-let [auth-data (extract-auth-data session)]
    (re-frame/dispatch [::token-refreshed auth-data])))

(defn on-signed-in [session]
  (when-let [auth-data (extract-auth-data session)]
    ;; dispatch signed-in
    (re-frame/dispatch [::signed-in auth-data])))

(defn on-signed-out []
  (re-frame/dispatch [::signed-out]))

(defn on-auth-failure [err]
  ;; dispatch auth failure
  (js/console.warn "Auth failure" err)
  (re-frame/dispatch [::signed-out]))

(defn maybeSignInWithRedirect [_ev]
  (get-auth-data
   (fn [session]
     (if-let [auth-data (extract-auth-data session)]
       (do
         (js/console.log "Already signed in - Session OK")
         (re-frame/dispatch [::token-refreshed auth-data]))
       (do
         (js/console.warn "No session - signInWithRedirect")
         (signInWithRedirect))))
   on-auth-failure))

(defn sign-out [_ev]
  (signOut))

(defn listen-to-auth-events [e]
  (let [event-name (js->event e)]
    (js/console.log "Listen" event-name)
    (cond
      (= "signedIn" event-name) (get-auth-data on-signed-in on-auth-failure)
      (= "signedOut" event-name) (on-signed-out)
      (= "signInWithRedirect" event-name) (prn "Sign in with redirect")
      (= "signInWithRedirect_failure" event-name) (on-auth-failure "Redirect failed")
      (= "tokenRefresh" event-name) (get-auth-data on-token-refreshed on-auth-failure)
      (= "tokenRefresh_failure" event-name) (on-auth-failure "Token refresh failed"))))

(defn init [cognito-config]
  (.setLanguage I18n "en")
  (.putVocabularies I18n (clj->js {"en" {"Sign in with AWS" "Sign in with Single Sign-On"}}))
  (.configure Amplify (clj->js {:Auth {:Cognito cognito-config}}))
  (.listen Hub "auth" listen-to-auth-events))

(defn configure [cognito-config]
  (if cognito-config
    (init cognito-config)
    (js/console.warn "Cognito not configured!!")))

;; ---------- events ----------

(re-frame/reg-event-fx
 ::token-refreshed
 (fn-traced
  [ctx [_ auth-data]]
  {:db (assoc (:db ctx) :auth-data auth-data)}))

(re-frame/reg-event-fx
 ::signed-in
 (fn-traced
  [ctx [_ auth-data]]
  {:db (assoc (:db ctx) :auth-data auth-data)}))

(re-frame/reg-event-fx
 ::signed-out
 (fn-traced
  [ctx _]
  {:navigate {:handler :authenticate}
   :db (-> (:db ctx)
           (dissoc :auth-data))}))

;; ---------- subscriptions ----------

(re-frame/reg-sub
 ::auth-data
 (fn [db _]
   (:auth-data db)))

(re-frame/reg-sub
 ::saml-error
 (fn [db _]
   (:saml-error db)))


;; ---------- views ----------

;; authenticate

(defn authenticate-panel []
  [:div
   [:h1 "Authenticate"]

   [:div
    [:button {:on-click maybeSignInWithRedirect} "Sign In"]]])

;; saml

(defn saml-panel []
  (let [auth-data (re-frame/subscribe [::auth-data])
        saml-error (re-frame/subscribe [::saml-error])]
    (when @saml-error
      [:div @saml-error])
    (if @auth-data
      (re-frame/dispatch [::routes/navigate {:handler :home} true])
      [:div
       [:h1 "Not authorized"]])))

;; not-authorized-panel

(defn not-authorized-panel []
  [:div
   [:h1 "Not authorized"]

   [:div
    [:button {:on-click #(maybeSignInWithRedirect %)}
     "Sign In"]]])

;; ---------- routes ----------

(defmethod routes/panels :authenticate-panel [] [authenticate-panel])
(defmethod routes/route-dispatcher :authenticate [cofx route] (routes/default-route-dispatcher cofx route))

(defmethod routes/panels :saml-panel [] [saml-panel])
(defmethod routes/route-dispatcher :saml [cofx route]
  (if-let [error (-> route :query-params :error)]
    {:db (assoc (:db cofx)
                :active-panel :saml-panel
                :saml-error error)}
    {:db (-> (:db cofx)
             (assoc :active-panel :saml-panel)
             (dissoc :saml-error))}))

(defmethod routes/panels :not-authorized-panel [] [not-authorized-panel])
(defmethod routes/route-dispatcher :not-authorized [cofx route] (routes/default-route-dispatcher cofx route))
