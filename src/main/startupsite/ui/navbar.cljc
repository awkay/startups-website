(ns startupsite.ui.navbar
  (:require
    [fulcro.client.mutations :as m :refer [defmutation]]
    #?@(:cljs [[fulcrologic.semantic-ui.factories :as s]
               [fulcrologic.semantic-ui.icons :as i]])
    [fulcro.client.primitives :as prim :refer [defsc]]
    [startupsite.ui.html5-routing :refer [nav-to!]]
    [fulcro.client.routing :as r :refer [defrouter]]))

(def nav-bar-ident [:component/main-nav-bar 1])
(defn nav-bar-field [f] (conj nav-bar-ident f))
(defn position-nav-bar* [state fixed?] (assoc-in state (nav-bar-field :ui/fixed?) fixed?))

(defmutation position-nav-bar
  "Mutation: Position the navbar as fixed or not."
  [{:keys [fixed?]}]
  (action [{:keys [state]}] (swap! state position-nav-bar* fixed?)))

(defn current-screen
  "Returns the current screen name for the top-level router"
  [props router-id]
  (some-> (r/current-route props router-id) first name keyword))

(defsc NavBar [this {:keys [ui/fixed?] :as props}]
  {:query         (fn [] [:ui/fixed? [r/routers-table '_]])
   :ident         (fn [] nav-bar-ident)
   :initial-state (fn [p] {:ui/fixed? false})}
  (let [options       (if fixed? #js {:fixed "top" :size "large"}
                                 #js {:inverted true :pointing true :secondary true :size "large"})
        active-screen (current-screen props :top-router)]
    #?(:cljs
       (s/ui-container nil
         (s/ui-menu options
           (s/ui-menu-item #js {:as      "a"
                                :onClick #(nav-to! this :home-page)
                                :active  (= :home-page active-screen)} "Home")
           (s/ui-menu-item #js {:onClick #(nav-to! this :submit-proposal)
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
          render?   (or (not fixed-only?) (= is-fixed? :fixed-only?))]
      (when render?
        (factory props)))))

