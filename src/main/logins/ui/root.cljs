(ns logins.ui.root
  (:require
    [fulcro.client.core :as fc]
    [fulcro.client.mutations :as m]
    [fulcro.client.logging :as log]
    [fulcro.client.data-fetch :as df]
    [logins.ui.components :refer [ui-placeholder]]
    translations.es                                         ; preload translations by requiring their namespace. See Makefile for extraction/generation
    [om.dom :as dom]
    [logins.api.mutations :as api]
    [fulcrologic.semantic-ui.factories :as s]
    [fulcrologic.semantic-ui.icons :as i]
    [react-facebook-login :refer [FacebookLogin]]
    [react-google-login :refer [GoogleLogin]]
    [react :refer [createElement]]
    [om.next :as om :refer [defui]]
    [fulcro.i18n :refer [tr trf]]))

(def ui-facebook-login (s/factory-apply (.-default FacebookLogin)))
(def ui-google-login (s/factory-apply (.-default GoogleLogin)))

(defui ^:once LocaleSelector
  static fc/InitialAppState
  (initial-state [c p] {:available-locales {"en" "English" "es" "Spanish"}})
  static om/Ident
  (ident [this props] [:ui-components/by-id :locale-selector])
  static om/IQuery
  ; the weird-looking query here pulls data from the root node (where the current locale is stored) with a "link" query
  (query [this] [[:ui/locale '_] :available-locales])
  Object
  (render [this]
    (let [{:keys [ui/locale available-locales]} (om/props this)]
      (dom/div nil "Locale:" (map (fn [[k v]]
                                    (dom/a #js {:href    "#"
                                                :style   #js {:paddingRight "5px"}
                                                :onClick #(om/transact! this `[(m/change-locale {:lang ~k})])} v)) available-locales)))))

(def ui-locale (om/factory LocaleSelector))

(def ui-menu-menu (s/sui-factory "Menu" "Menu") )

(defn ui-fixed-menu []
  (s/ui-menu #js {:fixed "top" :size "large"}
    (s/ui-container #js {}
      (s/ui-menu-item #js {:as "a" :active true} "Home")
      (s/ui-menu-item #js {:as "a"} "Work")
      (s/ui-menu-item #js {:as "a"} "Company")
      (s/ui-menu-item #js {:as "a"} "Careers")
      (ui-menu-menu #js {:position "right"}                    ; should be menu-menu
        (s/ui-menu-item #js {:className "item"}
          (s/ui-button #js {:as "a"} "Log in"))
        (s/ui-menu-item #js {:className "item"}
          (s/ui-button #js {:as "a"} "Sign Up"))))))

(defn show-fixed-menu [this] #(om/update-state! this assoc :visible? true))
(defn hide-fixed-menu [this] #(om/update-state! this assoc :visible? false))

(defn home-page [this]
  (let [visible? (om/get-state this :visible?)]
    (dom/div nil
      (when visible?
        (ui-fixed-menu))
      (s/ui-visibility #js {:onBottomPassed  (show-fixed-menu this)
                            :onBottomVisible (hide-fixed-menu this)
                            :once            false}
        (s/ui-segment #js {:inverted  true
                           :textAlign "center"
                           :style     #js {:minHeight 700 :padding "1em 0em"}}
          (s/ui-container nil
            (s/ui-menu #js {:inverted true :pointing true :secondary true :size "large"}
              (s/ui-menu-item #js {:as "a" :active true} "Home")
              (s/ui-menu-item #js {:as "a"} "Work")
              (s/ui-menu-item #js {:as "a"} "Company")
              (s/ui-menu-item #js {:as "a"} "Careers")
              (s/ui-menu-item #js {:as "a"}
                (s/ui-button #js {:as "a" :inverted true} "Log in")
                (s/ui-button #js {:as "a" :inverted true :style #js {:marginLeft "0.5em"}} "Sign Up"))))

          (s/ui-container #js {:text true}
            (s/ui-header #js {:as "h1" :content "Imaging-a-Company" :inverted true :style #js {:fontSize     "4em"
                                                                                               :fontWeight   "normal"
                                                                                               :marginBotton 0
                                                                                               :marginTop    "3em"}})
            (s/ui-header #js {:as "h2" :content "Do WTF" :inverted true :style #js {:fontSize   "1.7em"
                                                                                    :fontWeight "normal"}})

            (s/ui-button #js {:primary true :size "huge"}
              "Get Started"
              (s/ui-icon #js {:name "right arrow"})))))

      (s/ui-segment #js {:style #js {:padding "8em 0em"} :vertical true}
        (s/ui-grid #js {:container true :stackable true :verticalAlign "middle"}
          (s/ui-grid-row #js {}
            (s/ui-grid-column #js {:width 8}
              (s/ui-header #js {:as "h3" :style #js {:fontSize "2em"}} "We Help with Stuff")
              (dom/p #js {:style #js {:fontSize "1.33em"}} "Blah de blah blah blah lkjh alsdfjk lasdjfh laksjdf lkjahdf lajkhdf lkajhdfs lkjahdfs lkjahds lfkjhas ldkjfh alskdjfh lakjsdf lakjdshf lakjsdhf lkjashdf ")
              (s/ui-header #js {:as "h3" :style #js {:fontSize "2em"}} "We do other things, too!")
              (dom/p #js {:style #js {:fontSize "1.33em"}} "No one horse wonder here! ljkhads lfkjha sldfkjh alsdkhviovdauh vlakdjhoiuaerg difhv ldkjvh ldakfbjvodfiuhb ldskfbjv laskdvj alksd laskd vladsv"))
            (s/ui-grid-column #js {:width 6 :floated "right"}
              (s/ui-image #js {:bordered true :rounded true :size "large" }
                (ui-placeholder {:w 300 :h 300}))
              ))
          (s/ui-grid-row #js {}
            (s/ui-grid-column #js {:textAlign "center"}
              (s/ui-button #js {:size "huge"} "Check Them Out!")))))

      (s/ui-segment #js {:style #js {:padding "0em"} :vertical true}
        (s/ui-grid #js {:celled "internally" :columns "equal" :stackable true}
          (s/ui-grid-row #js {:textAlign "center"}
            (s/ui-grid-column #js {:style #js {:paddingBottom "5em" :paddingTop "5em"}}
              (s/ui-header #js {:as "h3" :style #js {:fontSize "2em"}} "Whaddacompany")
              (dom/p #js {:style #js {:fontSize "1.33em"}} "That's what they all say!"))
            (s/ui-grid-column #js {:style #js {:paddingBottom "5em" :paddingTop "5em"}}
              (s/ui-header #js {:as "h3" :style #js {:fontSize "2em"}} "I suck")
              (dom/p #js {:style #js {:fontSize "1.33em"}}
                (s/ui-image #js {:avatar true}
                  (ui-placeholder {:w 40 :h 40}))
                "A brilliant guy over there.")))))

      (s/ui-segment #js {:style #js {:padding "8em 0em"} :vertical true}
        (s/ui-container #js {:text true}
          (s/ui-header #js {:as "h3" :style #js {:fontSize "2em"}} "Breaking the grid, grabs your attn.")
          (dom/p #js {:style #js {:fontSize "1.33em"}}
            "Instead of doing shit we did shit.")
          (s/ui-button #js {:as "a" :size "large"} "Read More")
          (s/ui-divider #js {:as    "h4" :className "header" :horizontal true
                             :style #js {:margin "3em 0em" :textTransform "uppercase"}}
            (dom/a #js {:href "#"} "Case Studies"))
          (s/ui-header #js {:as "h2" :style #js {:fontSize "2em"}} "Did we Tell You Shit?")
          (dom/p #js {:style #js {:fondSize "2em"}} "Yeah yeah yeah...")
          (s/ui-button #js {:as "a" :size "large"} "Tell me More")))

      (s/ui-segment #js {:inverted true :vertical true :style #js {:padding "5em 0em"}}
        (s/ui-container nil
          (s/ui-grid #js {:divided true :inverted true :stackable true}
            (s/ui-grid-row nil
              (s/ui-grid-column #js {:width 3}
                (s/ui-header #js {:inverted true :as "h4" :content "About"})
                (s/ui-list #js {:link true :inverted true}
                  (s/ui-list-item #js {:as "a"} "Sitemap")
                  (s/ui-list-item #js {:as "a"} "Contact Us")
                  (s/ui-list-item #js {:as "a"} "Demons")
                  (s/ui-list-item #js {:as "a"} "Nuns")
                  ))
              (s/ui-grid-column #js {:width 3}
                (s/ui-header #js {:inverted true :as "h4" :content "Services"})
                (s/ui-list #js {:link true :inverted true}
                  (s/ui-list-item #js {:as "a"} "Hook-ups")
                  (s/ui-list-item #js {:as "a"} "Nose cleaning")
                  (s/ui-list-item #js {:as "a"} "Toe nails")
                  ))
              (s/ui-grid-column #js {:width 7}
                (s/ui-header #js {:inverted true :as "h4"} "Footer Header")
                (dom/p nil "Extra space for shit")))))))))

(defui ^:once Root
  static fc/InitialAppState
  (initial-state [c p] {:ui/locale-selector (fc/get-initial-state LocaleSelector {})})
  static om/IQuery
  (query [this] [:ui/locale :ui/react-key {:ui/locale-selector (om/get-query LocaleSelector)}])
  Object
  (render [this]
    (let [{:keys [ui/react-key ui/main ui/locale-selector] :or {react-key "ROOT"}} (om/props this)]
      (dom/div #js {:key react-key}
        ;(ui-locale locale-selector)
        (home-page this)
        ))))



