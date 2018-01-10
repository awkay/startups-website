(ns startupsite.login-cards
  (:require
    [devcards.core :refer [defcard]]
    [fulcro.client.cards :refer [defcard-fulcro]]
    [fulcro.client.primitives :as prim :refer [defsc]]
    [fulcro.client.mutations :refer [defmutation]]
    [fulcro.client.data-fetch :as df]
    [startupsite.card-utils :refer [semantic-ui-frame]]
    [fulcrologic.semantic-ui.factories :as s]
    [fulcro.client.dom :as dom]
    [fulcro-css.css :as css]
    [fulcrologic.semantic-ui.icons :as i]
    [clojure.string :as str]
    [fulcro.client.mutations :as m]))

(defn non-empty-string? [s] (pos? (and s (string? s) (count (str/trim s)))))

(defsc User [t p]
  {:query [:user/id :user/name :user/valid?]
   :ident (fn [] [:session :user])})

(defn set-login-progress* [state-map in-progress?]
  (assoc-in state-map [:ui/forms :login-form :login/in-progress?] in-progress?))

(defn set-login-message* [state-map message]
  (assoc-in state-map [:ui/forms :login-form :login/message] message))

(defn session-valid? [state-map] (true? (get-in state-map [:session :user :user/valid?] false)))

(defmutation login [params]
  (action [{:keys [state]}] (swap! state set-login-progress* true))
  (remote [{:keys [ast state] :as env}] (m/returning ast state User)))

(defmutation handle-login-network-error [params]
  (action [{:keys [state]}]
    (swap! state (fn [s]
                   (-> s
                     (set-login-message* "We're experiencing network problems. Try again later.")
                     (set-login-progress* true))))))

(defmutation process-login-result [{:keys [onComplete]}]
  (action [{:keys [state reconciler]}]
    (swap! state (fn [s] (-> s
                           (set-login-progress* false))))
    (when (and (session-valid? @state) (symbol? onComplete))
      (prim/ptransact! reconciler `[(~onComplete)]))))

(defsc LoginForm [this {:keys [login/email login/password login/in-progress? login/message] :as props} _ {:keys [column image grid]}]
  {:query         [:login/email :login/password :login/in-progress? :login/message]
   :css           [[:.column {:max-witdh "450px"}]
                   [:.grid {:height "100%"}]
                   [:.image {:margin-top "-100px"}]]
   :ident         (fn [] [:ui/forms :login-form])
   :initial-state {:login/email        "" :login/password ""
                   :login/in-progress? false :login/message ""}}
  (let [error? (and (not in-progress?) (non-empty-string? message))]
    (dom/div #js {:className (str "ui middle aligned center grid " grid)}
      (dom/div #js {:className column}
        (dom/h2 #js {:className (str "ui teal header " image)}
          (dom/img #js {:width 200 :src "/img/fulcrologic.png", :className image})
          (dom/div #js {:className "content"} "\n Log-in to your account\n "))
        (s/ui-form #js {:size "large" :loading in-progress?}
          (dom/div #js {:className "ui stacked segment"}
            (dom/div #js {:className "field"}
              (dom/div #js {:className "ui left icon input"}
                (dom/i #js {:className "user icon"})
                (dom/input #js {:type        "text"
                                :value       email
                                :name        "email"
                                :placeholder "E-mail address"})))
            (s/ui-form-field nil
              (dom/div #js {:className "ui left icon input"}
                (dom/i #js {:className "lock icon"})
                (dom/input #js {:type  "password",
                                :value password
                                :name  "password", :placeholder "Password"})))
            (s/ui-button #js {:fluid   true :size "large" :color "teal"
                              :onClick #(prim/ptransact! this `[(login {:email email :password password})
                                                                (df/fallback {:action handle-login-network-error})
                                                                (process-login-result {})])} "Login")
            (s/ui-message #js {:visible error? :error true}
              (s/ui-message-content #js {} message))))

        (dom/div #js {:className "ui message"} "New to us? "
          (dom/a #js {:href "#"} "Sign Up"))))))

(def ui-login-form (prim/factory LoginForm {:keyfn :db/id}))

(defsc Root [this {:keys [ROOT/login-form]}]
  {:query         [{:ROOT/login-form (prim/get-query LoginForm)}]
   :css-include   [LoginForm]
   :initial-state {:ROOT/login-form {}}}
  (semantic-ui-frame "800px" "400px"
    (css/style-element Root)
    (ui-login-form login-form)))

(def ui-root (prim/factory Root))

(defcard-fulcro login-component-unfilled
  Root)

(defcard-fulcro login-component-partially-filled
  Root
  {}
  {:fulcro
   {:started-callback
    (fn [{:keys [reconciler]}]
      (js/console.log :LOGIN)
      (prim/merge-component! reconciler LoginForm {:login/email    "jo@example.net"
                                                   :login/password "letmein"}))}})

(defcard-fulcro login-component-error-message
  Root
  {}
  {:fulcro
   {:started-callback
    (fn [{:keys [reconciler]}]
      (js/console.log :LOGIN)
      (prim/merge-component! reconciler LoginForm {:login/email    "jo@example.net"
                                                   :login/message  "Invalid credentials."
                                                   :login/password "letmein"}))}})

(defcard-fulcro login-component-in-progress
  Root
  {}
  {:inspect-data true
   :fulcro       {:started-callback
                  (fn [{:keys [reconciler]}]
                    (js/console.log :LOGIN)
                    (prim/merge-component! reconciler LoginForm {:login/email        "jo@example.net"
                                                                 :login/in-progress? true
                                                                 :login/password     "letmein"}))}})
