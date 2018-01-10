(ns startupsite.ui.login
  (:require
    [fulcro.client.primitives :as prim :refer [defsc]]
    [fulcro.client.mutations :refer [defmutation]]
    [fulcro.client.data-fetch :as df]
    [fulcrologic.semantic-ui.factories :as s]
    [fulcro.client.dom :as dom]
    [fulcro-css.css :as css]
    [fulcro.server :as server]
    [fulcrologic.semantic-ui.icons :as i]
    [clojure.string :as str]
    [fulcro.client.mutations :as m]))

(defn non-empty-string? [s] (pos? (and s (string? s) (count (str/trim s)))))

(defsc User [t p]
  {:query [:user/id :user/name :user/valid?]
   :ident (fn [] [:session :user])})

(defn clear-session* [state-map] (assoc-in state-map [:session :user] {}))

(defn set-login-progress* [state-map in-progress?]
  (assoc-in state-map [:ui/forms :login-form :login/in-progress?] in-progress?))

(defn set-login-message* [state-map message]
  (assoc-in state-map [:ui/forms :login-form :login/message] message))

(defn session-valid? [state-map] (true? (get-in state-map [:session :user :user/valid?] false)))

(letfn [(credentials-supplied? [state-map]
          (let [{:keys [login/email login/password]} (get-in state-map [:ui/forms :login-form])]
            (and
              (non-empty-string? email)
              (non-empty-string? password))))]
  (defmutation login [params]
    (action [{:keys [state]}]
      (if (credentials-supplied? @state)
        (swap! state set-login-progress* true)
        (swap! state (fn [s]
                       (-> s
                         (clear-session*)
                         (set-login-message* "Email and password required."))))))
    (remote [{:keys [ast state] :as env}]
      (when (credentials-supplied? @state)
        (m/returning ast state User)))
    (refresh [env]
      [:login/in-progress? :login/message])))

(server/defmutation login
  "Process logins on server. Returns a value session description User."
  [{:keys [email password]}]
  (action [env]
    {:user/id 1 :user/name "Tony" :user/valid? true}))

(defmutation handle-login-network-error [params]
  (action [{:keys [state]}]
    (swap! state (fn [s]
                   (-> s
                     (set-login-message* "We're experiencing network problems. Try again later.")
                     (set-login-progress* false))))))

(defmutation process-login-result [{:keys [onComplete]}]
  (action [{:keys [state reconciler]}]
    #?(:cljs (js/console.log :s @state))
    (swap! state (fn [s] (cond-> (set-login-progress* s false)
                           (session-valid? @state) (set-login-message* ""))))
    (when (and (session-valid? @state) (symbol? onComplete))
      (prim/ptransact! reconciler `[(~onComplete)]))))

(defsc LoginForm [this {:keys [login/email login/password login/in-progress? login/message] :as props} _ {:keys [column image grid]}]
  {:query         [:login/email :login/password :login/in-progress? :login/message]
   :css           [[:.column {:max-width "450px"}]
                   [:.grid {:height "100%"}]]
   :ident         (fn [] [:ui/forms :login-form])
   :initial-state {:login/email        "" :login/password ""
                   :login/in-progress? false :login/message ""}}
  (let [error? (and (not in-progress?) (non-empty-string? message))]
    (dom/div #js {:className (str "ui middle aligned centered grid " grid)}
      (dom/div #js {:className (str "column " column)}
        (dom/h2 #js {:className (str "ui teal header")}
          (dom/img #js {:width 200 :src "/img/fulcrologic.png"})
          (dom/div #js {:className "content"} "\n Log-in to your account\n "))
        (s/ui-form #js {:size "large" :loading in-progress?}
          (s/ui-segment #js {:stacked true}
            (s/ui-form-field nil
              (s/ui-input #js {:icon true :iconPosition "left"}
                (s/ui-icon #js {:name i/user-icon})
                (dom/input #js {:type        "text"
                                :value       email
                                :onChange    (fn [evt] (m/set-string! this :login/email :event evt))
                                :name        "email"
                                :placeholder "E-mail address"})))
            (s/ui-form-field nil
              (s/ui-input #js {:icon true :iconPosition "left"}
                (s/ui-icon #js {:name i/lock-icon})
                (dom/input #js {:type     "password",
                                :value    password
                                :onChange (fn [evt] (m/set-string! this :login/password :event evt))
                                :name     "password", :placeholder "Password"})))
            (s/ui-button #js {:fluid   true :size "large" :color "teal"
                              :onClick #(prim/ptransact! this `[(login {:email ~email :password ~password})
                                                                (df/fallback {:action handle-login-network-error})
                                                                (process-login-result {})])} "Login")
            (s/ui-message #js {:visible error? :error true}
              (s/ui-message-content #js {} message))))

        (s/ui-message nil
          (s/ui-message-content nil
            "Don't have a login? "
            (dom/a #js {:href "#"} "Sign Up.")))))))

(def ui-login-form (prim/factory LoginForm {:keyfn :db/id}))
