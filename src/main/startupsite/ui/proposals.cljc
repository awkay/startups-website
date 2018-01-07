(ns startupsite.ui.proposals
  (:require
    [fulcro.client.mutations :as m :refer [defmutation]]
    [startupsite.ui.components :refer [ui-placeholder ScrollTarget ui-scroll-target]]
    translations.es                                         ; preload translations by requiring their namespace. See Makefile for extraction/generation
    [fulcro.client.dom :as dom]
    #?@(:cljs [[fulcrologic.semantic-ui.factories :as s]
               [fulcrologic.semantic-ui.icons :as i]])
    [fulcro.client.primitives :as prim :refer [defsc]]
    [startupsite.ui.screen-utils :refer [screen-type screen-ident screen-initial-state screen-query]]
    [startupsite.ui.navbar :as nav :refer [NavBar]]))

(defsc SubmitProposal [this {:keys [ui/proposal-form ui/main-nav-bar] :as props}]
  {:query         (fn [] (screen-query :submit-proposal
                           [:ui/proposal-form {[:ui/main-nav-bar '_] (prim/get-query NavBar)}]))
   :ident         (fn [] (screen-ident :submit-proposal))
   :initial-state (fn [params]
                    (screen-initial-state :submit-proposal
                      {}))}
  #?(:cljs
     (dom/div nil
       (s/ui-visibility #js {:onBottomPassed  #(prim/transact! this `[(nav/position-nav-bar {:fixed? true})])
                             :onBottomVisible #(prim/transact! this `[(nav/position-nav-bar {:fixed? false})])
                             :once            false}
         (s/ui-segment #js {:inverted true}
           (nav/ui-nav-bar main-nav-bar :fixed-only? true)
           (nav/ui-nav-bar main-nav-bar))
         (s/ui-segment nil
           "Submit a proposal...")))))
