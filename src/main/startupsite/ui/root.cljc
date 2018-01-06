(ns startupsite.ui.root
  (:require
    [fulcro.client :as fc]
    [fulcro.client.mutations :as m]
    [fulcro.client.logging :as log]
    [fulcro.client.data-fetch :as df]
    [startupsite.ui.components :refer [ui-placeholder]]
    translations.es                                         ; preload translations by requiring their namespace. See Makefile for extraction/generation
    [fulcro.client.dom :as dom]
    [startupsite.api.mutations :as api]
    #?@(:cljs [[fulcrologic.semantic-ui.factories :as s]
               [fulcrologic.semantic-ui.icons :as i]])
    [fulcro.client.primitives :as prim :refer [defsc]]
    [fulcro.i18n :refer [tr trf]]))

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
                            (s/ui-button #js {:as "a" :primary true} "Sign Up"))))))))

(defn show-fixed-menu [this] #(prim/update-state! this assoc :visible? true))
(defn hide-fixed-menu [this] #(prim/update-state! this assoc :visible? false))

(defn home-page [this]
  (let [visible? (prim/get-state this :visible?)]
    #?(:clj (dom/div nil "")
       :cljs
            (dom/div nil
              (when visible? (ui-fixed-menu))
              (s/ui-visibility #js {:onBottomPassed  (show-fixed-menu this)
                                    :onBottomVisible (hide-fixed-menu this)
                                    :once            false}
                (s/ui-segment #js {:inverted  true
                                   :textAlign "center"
                                   :style     #js {:minHeight 700 :padding "1em 0em"}}
                  (s/ui-container nil
                    (s/ui-menu #js {:inverted true :pointing true :secondary true :size "large"}
                      (s/ui-menu-item #js {:as "a" :active true} "Home")
                      (s/ui-menu-item #js {:as "a"} "Send a Proposal")
                      (s/ui-menu-item #js {:as "a"} "Company")
                      (s/ui-menu-item #js {:as "a"} "Careers")
                      (comment (s/ui-menu-item #js {:position "right"}
                                 (s/ui-button #js {:as "a" :inverted true} "Log in")
                                 (s/ui-button #js {:as "a" :inverted true :style #js {:marginLeft "0.5em"}} "Sign Up")))))

                  (s/ui-container #js {:text true}
                    (s/ui-header #js {:as    "h1" :content "Fulcrologic" :inverted true
                                      :style #js {:fontSize "4em" :fontWeight "normal" :marginBotton 0 :marginTop "3em"}})
                    (s/ui-header #js {:as    "h2" :content "Starting off Right" :inverted true
                                      :style #js {:fontSize "1.7em" :fontWeight "normal"}})
                    (s/ui-button #js {:primary true :size "huge"}
                      "Get Started"
                      (s/ui-icon #js {:name "right arrow"})))))

              (s/ui-segment #js {:style #js {:padding "8em 0em"} :vertical true}
                (s/ui-grid #js {:container true :stackable true :verticalAlign "middle"}
                  (s/ui-grid-row #js {}
                    (s/ui-grid-column #js {:width 8}
                      (s/ui-header #js {:as "h3" :style #js {:fontSize "2em"}} "We Help You Get Started!")
                      (dom/p #js {:style #js {:fontSize "1.33em"}} "Our startups division is all about helping entreprenuers get the software they need, at a price they can afford.")
                      (s/ui-header #js {:as "h3" :style #js {:fontSize "2em"}} "Shared Risks, Shared Rewards")
                      (dom/p #js {:style #js {:fontSize "1.33em"}} "We work with you at a reduced rate to produce a well-written, minimally viable product, with which you can
               launch your business. As you start to generate revenue, you make easy payments on the balance to eventually own your IP! If you fail, you owe us nothing more."))
                    (s/ui-grid-column #js {:width 6 :floated "right"}
                      (s/ui-image #js {:bordered true :rounded true :size "large"}
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
                      (dom/p #js {:style #js {:fontSize "1.33em"}}
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
                        (dom/p nil "Extra space for stuff coming soon..."))))))))))

(defsc Root [this props]
  {:query []}
  (dom/div nil
    (home-page this)))



