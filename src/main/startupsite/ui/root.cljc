(ns startupsite.ui.root
  (:require
    [fulcro.client :as fc]
    [fulcro.client.mutations :as m :refer [defmutation]]
    [fulcro.client.logging :as log]
    [fulcro.client.data-fetch :as df]
    [startupsite.ui.components :as components :refer [ui-placeholder ScrollTarget ui-scroll-target]]
    translations.es                                         ; preload translations by requiring their namespace. See Makefile for extraction/generation
    [fulcro.client.dom :as dom]
    [startupsite.api.mutations :as api]
    #?@(:cljs [[fulcrologic.semantic-ui.factories :as s]
               [fulcrologic.semantic-ui.icons :as i]])
    [fulcro.client.primitives :as prim :refer [defsc]]
    [fulcro.client.routing :as r :refer [defrouter]]
    [fulcro.i18n :refer [tr trf]]))

(declare TopRouter)

#?(:clj
   (def clj->js identity))

(defn screen-type
  "Returns the keyword that corresponds to the table name for the given screen name"
  [screen-name] (keyword "screen" (name screen-name)))

(defn screen-ident
  "Returns the ident for the screen with the given name."
  [screen-name-or-props]
  (if (map? screen-name-or-props)
    (screen-ident (:screen/table screen-name-or-props))
    [(screen-type screen-name-or-props) 1]))

(defn screen-initial-state
  "Returns the initial state for a screen with the given name and initial state (adding in the proper ident-resolution data)"
  [screen-name state]
  (merge state {:screen/table (screen-type screen-name)}))

(defn screen-query [screen-name query]
  (into query [:screen/table]))

(defsc RouterInfo [this props]
  {:query [::r/id ::r/current-route]
   :ident [:fulcro.client.routing.routers/by-id ::r/id]})

(comment
  #?(:cljs (def ui-menu-menu (s/sui-factory "Menu" "Menu")))

  (defn ui-fixed-menu []
    #?(:cljs (s/ui-menu #js {:fixed "top" :size "large"}
               (s/ui-container #js {}
                 (s/ui-menu-item #js {:as "a" :active true} "Home")
                 (s/ui-menu-item #js {:as "a"} "Send a Proposal")
                 (s/ui-menu-item #js {:as "a"} "Company")
                 (s/ui-menu-item #js {:as "a"} "Careers")
                 (comment (ui-menu-menu #js {:position "right"} ; should be menu-menu
                            (s/ui-menu-item #js {:className "item"}
                              (s/ui-button #js {:as "a"} "Log in"))
                            (s/ui-menu-item #js {}
                              (s/ui-button #js {:as "a" :primary true} "Sign Up")))))))))

(def nav-bar-ident [:component/main-nav-bar 1])
(defn nav-bar-field [f] (conj nav-bar-ident f))
(defn position-nav-bar* [state fixed?] (assoc-in state (nav-bar-field :ui/fixed?) fixed?))

(defmutation position-nav-bar
  "Mutation: Position the navbar as fixed or not."
  [{:keys [fixed?]}]
  (action [{:keys [state]}] (swap! state position-nav-bar* fixed?)))

(defn current-screen
  "Returns the current screen name for the top-level router"
  [router]
  (some-> router
    ::r/current-route
    first
    name
    keyword))

(defsc NavBar [this {:keys [ui/fixed? ui/top-router] :as props}]
  {:query         [:ui/fixed? {[:ui/top-router '_] (prim/get-query RouterInfo)}]
   :ident         (fn [] nav-bar-ident)
   :initial-state {:ui/fixed? false}}
  (let [options       (clj->js (if fixed? {:fixed "top" :size "large"}
                                          {:inverted true :pointing true :secondary true :size "large"}))
        active-screen (current-screen top-router)]
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
          render?   (or (not fixed-only?) (= is-fixed? :fixed-only?))]
      (when render?
        (factory props)))))

(defsc HomePage [this {:keys [ui/main-nav-bar ui/get-started-target]}]
  {:query         (fn [] (screen-query :home-page
                           [{:ui/get-started-target (prim/get-query ScrollTarget)}
                            {[:ui/main-nav-bar '_] (prim/get-query NavBar)}]))
   :ident         (fn [] (screen-ident :home-page))
   :initial-state (fn [params] (screen-initial-state :home-page
                                 {:ui/get-started-target (prim/get-initial-state ScrollTarget {:id :get-started})}))}
  #?(:clj (dom/div nil "")
     :cljs
          (dom/div nil
            (ui-nav-bar main-nav-bar :fixed-only? true)
            (s/ui-visibility #js {:onBottomPassed  #(prim/transact! this `[(position-nav-bar {:fixed? true})])
                                  :onBottomVisible #(prim/transact! this `[(position-nav-bar {:fixed? false})])
                                  :once            false}
              (s/ui-segment #js {:inverted  true
                                 :textAlign "center"
                                 :style     #js {:minHeight 700 :padding "1em 0em"}}
                (ui-nav-bar main-nav-bar)

                (s/ui-container #js {:text true}
                  (s/ui-header #js {:as    "h1" :content "Fulcrologic" :inverted true
                                    :style #js {:fontSize "4em" :fontWeight "normal" :marginBotton 0 :marginTop "3em"}})
                  (s/ui-header #js {:as    "h2" :content "Starting off Right" :inverted true
                                    :style #js {:fontSize "1.7em" :fontWeight "normal"}})
                  (s/ui-button #js {:primary true :size "huge"
                                    :onClick #(prim/transact! this `[(components/scroll-to {:target :get-started})])}
                    "Get Started"
                    (s/ui-icon #js {:name "right arrow"})))))

            (ui-scroll-target get-started-target)
            (s/ui-segment #js {:style #js {:padding "8em 0em"} :vertical true}
              (s/ui-grid #js {:container true :stackable true :verticalAlign "middle"}
                (s/ui-grid-row #js {}
                  (s/ui-grid-column #js {:width 8}
                    (s/ui-header #js {:as "h3" :style #js {:fontSize "2em"}} "We Help You Get Started!" )
                    (dom/p #js {:style #js {:fontSize "1.33em"}} "Our startups division is all about helping entreprenuers get the software they need, at a price they can afford.")
                    (s/ui-header #js {:as "h3" :style #js {:fontSize "2em"}} "Shared Risks, Shared Rewards")
                    (dom/p #js {:style #js {:fontSize "1.33em"}} "We work with you at a reduced rate to produce a well-written, minimally viable product, with which you can
               launch your business. As you start to generate revenue, you make easy payments on the balance to eventually own your IP! If you fail, you owe us nothing more."))
                  (s/ui-grid-column #js {:width 6 :floated "right"}
                    (s/ui-image #js {:bordered true :shape "rounded" :size "large"}
                      (ui-placeholder {:w 300 :h 300}))))
                (s/ui-grid-row #js {}
                  (s/ui-grid-column #js {:textAlign "center"}
                    (s/ui-button #js {:size "huge"} "Read More!")))))

            (s/ui-segment #js {:style #js {:padding "0em"} :vertical true}
              (s/ui-grid #js {:celled "internally" :columns "equal" :stackable true}
                (s/ui-grid-row #js {:textAlign "center"}
                  (s/ui-grid-column #js {:style #js {:paddingBottom "5em" :paddingTop "5em"}}
                    (s/ui-header #js {:as "h3" :style #js {:fontSize "2em"}} "My attempts at hiring an expert were expensive, and also a disaster. Fulcrologic really makes this less risky!")
                    (dom/p #js {:style #js {:fontSize "1.33em"}} "That's what they all say!"))
                  (s/ui-grid-column #js {:style #js {:paddingBottom "5em" :paddingTop "5em"}}
                    (s/ui-header #js {:as "h3" :style #js {:fontSize "2em"}} "Wow, software I can actually afford?")
                    (dom/div #js {:style #js {:fontSize "1.33em"}}
                      (s/ui-image #js {:avatar true}
                        (ui-placeholder {:w 40 :h 40}))
                      "A brilliant guy over there.")))))

            (s/ui-segment #js {:style #js {:padding "8em 0em"} :vertical true}
              (s/ui-container #js {:text true}
                (s/ui-header #js {:as "h3" :style #js {:fontSize "2em"}} "How it Works")
                (dom/p #js {:style #js {:fontSize "1.33em"}}
                  "More coming soon!")
                (s/ui-button #js {:as "a" :size "large"} "Read More")
                (comment
                  (s/ui-divider #js {:as    "h4" :className "header" :horizontal true
                                     :style #js {:margin "3em 0em" :textTransform "uppercase"}}
                    (dom/a #js {:href "#"} "Case Studies"))
                  (s/ui-header #js {:as "h2" :style #js {:fontSize "2em"}} "Did we Tell You Shit?")
                  (dom/p #js {:style #js {:fondSize "2em"}} "Yeah yeah yeah...")
                  (s/ui-button #js {:as "a" :size "large"} "Tell me More"))))

            (s/ui-segment #js {:inverted true :vertical true :style #js {:padding "5em 0em"}}
              (s/ui-container nil
                (s/ui-grid #js {:divided true :inverted true :stackable true}
                  (s/ui-grid-row nil
                    (s/ui-grid-column #js {:width 3}
                      (s/ui-header #js {:inverted true :as "h4" :content "More soon..."})
                      (s/ui-list #js {:link true :inverted true}
                        (s/ui-list-item #js {:as "a"} "A")
                        (s/ui-list-item #js {:as "a"} "B")))
                    (s/ui-grid-column #js {:width 3}
                      (s/ui-header #js {:inverted true :as "h4" :content "Services"})
                      (s/ui-list #js {:link true :inverted true}
                        (s/ui-list-item #js {:as "a"} "Startup Software")
                        (s/ui-list-item #js {:as "a"} "Management Consulting")
                        (s/ui-list-item #js {:as "a"} "Apprentices")))
                    (s/ui-grid-column #js {:width 7}
                      (s/ui-header #js {:inverted true :as "h4"} "Footer Header")
                      (dom/p nil "Extra space for stuff coming soon...")))))))))

(def ui-home-page (prim/factory HomePage))

(defsc SubmitProposal [this {:keys [ui/proposal-form ui/main-nav-bar] :as props}]
  {:query         (fn [] (screen-query :submit-proposal
                           [:ui/proposal-form {[:ui/main-nav-bar '_] (prim/get-query NavBar)}]))
   :ident         (fn [] (screen-ident :submit-proposal))
   :initial-state (fn [params]
                    (screen-initial-state :submit-proposal
                      {}))}
  #?(:cljs
     (dom/div nil
       (s/ui-visibility #js {:onBottomPassed  #(prim/transact! this `[(position-nav-bar {:fixed? true})])
                             :onBottomVisible #(prim/transact! this `[(position-nav-bar {:fixed? false})])
                             :once            false}
         (s/ui-segment #js {:inverted true}
           (ui-nav-bar main-nav-bar :fixed-only? true)
           (ui-nav-bar main-nav-bar))
         (s/ui-segment nil
           "Submit a proposal...")))))

(def ui-submit-proposal (prim/factory SubmitProposal {:keyfn :screen/table}))

(defrouter TopRouter :top-router
  (ident [this props] (screen-ident props))
  :screen/home-page HomePage
  :screen/submit-proposal SubmitProposal
  )

(def ui-top-router (prim/factory TopRouter))

(def routing-tree
  (r/routing-tree
    (r/make-route :home-page [(r/router-instruction :top-router (screen-ident :home-page))])
    (r/make-route :submit-proposal [(r/router-instruction :top-router (screen-ident :submit-proposal))])
    ))

(defsc Root [this {:keys [ui/top-router]}]
  {:initial-state (fn [params]
                    (merge routing-tree
                      {:ui/main-nav-bar (prim/get-initial-state NavBar {})}
                      {:ui/top-router (prim/get-initial-state TopRouter {})}))
   :query         [{:ui/main-nav-bar (prim/get-query NavBar)}
                   {:ui/top-router (prim/get-query TopRouter)}]}
  (ui-top-router top-router))



