(ns startupsite.ui.navbar
  (:require
    [fulcro.client :as fc]
    [fulcro.client.mutations :as m :refer [defmutation]]
    [fulcro.client.logging :as log]
    [fulcro.client.data-fetch :as df]
    [startupsite.ui.components :as components :refer [ui-placeholder ScrollTarget ui-scroll-target]]
    translations.es                                         ; preload translations by requiring their namespace. See Makefile for extraction/generation
    [fulcro.client.dom :as dom]
    #?@(:cljs [[fulcrologic.semantic-ui.factories :as s]
               [fulcrologic.semantic-ui.icons :as i]])
    [fulcro.client.primitives :as prim :refer [defsc]]
    [fulcro.client.routing :as r :refer [defrouter]]
    [startupsite.ui.screen-utils :refer [screen-type screen-ident screen-initial-state screen-query clj->js]]
    [fulcro.i18n :refer [tr trf]]))

(defsc RouterInfo [this props]
  {:query [::r/id ::r/current-route]
   :ident [:fulcro.client.routing.routers/by-id ::r/id]})

(def nav-bar-ident [:component/main-nav-bar 1])
(defn nav-bar-field [f] (conj nav-bar-ident f))
(defn position-nav-bar* [state fixed?] (assoc-in state (nav-bar-field :ui/fixed?) fixed?))

(defmutation position-nav-bar
  "Mutation: Position the navbar as fixed or not."
  [{:keys [fixed?]}]
  (action [{:keys [state]}] (swap! state position-nav-bar* fixed?)))

(defn current-screen
  "Returns the current screen name for the top-level router"
  [router-table router-id]
  (some-> router-table
    (get router-id)
    ::r/current-route
    first
    name
    keyword))

(defsc NavBar [this {:keys [ui/fixed? r/routers-table] :as props}]
  {:query         (fn [] [:ui/fixed? [r/routers-table '_]])
   :ident         (fn [] nav-bar-ident)
   :initial-state (fn [p] {:ui/fixed? false})}
  (let [options (clj->js (if fixed? {:fixed "top" :size "large"}
                                    {:inverted true :pointing true :secondary true :size "large"}))
        active-screen :home #_(current-screen routers-table :top-router )]
    #?(:cljs
       (s/ui-container nil
         (s/ui-menu options
           (s/ui-menu-item #js {:as      "a"
                                :onClick #(prim/transact! this `[(r/route-to {:handler :home-page})
                                                                 :ui/top-router])
                                :active  (= :home-page active-screen)} "Home")
           (s/ui-menu-item #js {:onClick #(prim/transact! this `[(r/route-to {:handler :submit-proposal})
                                                                 :ui/top-router])
                                :active  (= :submit-proposal active-screen)
                                :as      "a"} "Send a Proposal")
           (s/ui-menu-item #js {:as "a"} "Company")
           (s/ui-menu-item #js {:as "a"} "Careers")
           (comment
             (s/ui-menu-item #js {:position "right"}
               (s/ui-button #js {:as "a" :inverted true} "Log in")
               (s/ui-button #js {:as "a" :inverted true :style #js {:marginLeft "0.5em"}} "Sign Up"))))))))

(let [factory (prim/factory NavBar)]
  (defn ui-nav-bar [props & {:keys [fixed-only?] :or {fixed-only? false}}]
    (let [is-fixed? (:ui/fixed? props false)
          render? (or (not fixed-only?) (= is-fixed? :fixed-only?))]
      (when render?
        (factory props)))))

