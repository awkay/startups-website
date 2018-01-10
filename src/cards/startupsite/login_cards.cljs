(ns startupsite.login-cards
  (:require-macros [devcards.core :refer [defcard]])
  (:require
    [devcards.core :as dc]
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
    [startupsite.ui.login :refer [ui-login-form LoginForm]]
    [fulcro.client.mutations :as m]))

(defsc Root [this {:keys [ROOT/login-form] :as props}]
  {:query         [[:session :user] {:ROOT/login-form (prim/get-query LoginForm)}]
   :css-include   [LoginForm]
   :initial-state {:ROOT/login-form {}}}
  (let [{:keys [user/name user/valid?]} (get props [:session :user])]
    (semantic-ui-frame "800px" "400px"
      (css/style-element Root)
      (dom/div nil
        (when valid?
          (s/ui-container nil
            (dom/p nil "Logged in as " name)))
        (ui-login-form login-form)))))

(def ui-root (prim/factory Root))

(defcard-fulcro login-component-active
  Root
  {}
  {:inspect-data true})

(defcard login-component-unfilled
  (semantic-ui-frame "800px" "400px"
    (css/style-element Root)
    (ui-login-form {})))

(defcard login-component-filled
  (semantic-ui-frame "800px" "400px"
    (css/style-element Root)
    (ui-login-form {:login/email    "jo@example.net"
                    :login/password "letmein"})))


(defcard login-component-error-message
  (semantic-ui-frame "800px" "400px"
    (css/style-element Root)
    (ui-login-form {:login/email    "jo@example.net"
                    :login/message  "Invalid credentials."
                    :login/password "letmein"})))

(defcard login-component-in-progress
  (semantic-ui-frame "800px" "400px"
    (css/style-element Root)
    (ui-login-form {:login/email        "jo@example.net"
                    :login/in-progress? true
                    :login/password     "letmein"})))
