(ns startupsite.ui.proposals
  (:require
    [fulcro.client.dom :as dom]
    [fulcrologic.semantic-ui.factories :as s]
    [fulcrologic.semantic-ui.icons :as i]
    [fulcro.client.mutations :as m :refer [defmutation]]
    [fulcro.client.primitives :as prim :refer [defsc]]))

(defn form-input [id label value required?]
  (let [input-id (str id "-input")]
    (s/ui-form-field #js {:required required?}
      (dom/label #js {:htmlFor input-id} label)
      (dom/input #js {:id   input-id :value value
                      :type "text"}))))

(defsc RegistrationForm [this props]
  {}
  (s/ui-form nil
    (form-input "company" "Company Name" "" true)
    (form-input "contact" "Contact Name" "" true)
    (form-input "email" "Email" "" true)
    (form-input "phone" "Phone Number" "" true)))

(def ui-registration-form (prim/factory RegistrationForm))

(defn answer-question* [state-map id value]
  (assoc-in state-map [:survey-answer/by-id id :survey-answer/value] value))

(defmutation answer-question [{:keys [id value]}]
  (action [{:keys [state]}]
    (swap! state answer-question* id value))
  (refresh [env]
    [:survey/questions]))

(defmulti render-question-input (fn [component id type label value] type))

(defmethod render-question-input :boolean [this id type label value]
  (s/ui-form nil
    (s/ui-form-group #js {:grouped true}
      (dom/label nil label)
      (s/ui-form-field #js {:inline true}
        (dom/label nil "Yes")
        (dom/input #js {:name     id
                        :onChange (fn [] (prim/transact! this `[(answer-question {:id ~id :value true})]))
                        :checked  (identical? value true) :type "radio"}))
      (s/ui-form-field #js {:inline true}
        (dom/label nil "No")
        (dom/input #js {:name     id
                        :onChange (fn [] (prim/transact! this `[(answer-question {:id ~id :value false})]))
                        :checked  (identical? value false) :type "radio"})))))

(defsc SurveyAnswer [this {:keys [db/id survey-answer/value] :as props} {:keys [type label]}]
  {:query         [:db/id :survey-answer/value]
   :ident         [:survey-answer/by-id :db/id]
   :initial-state {:db/id :param/id}}
  (render-question-input this id type label value))

(let [factory (prim/factory SurveyAnswer {:keyfn :db/id})]
  (defn ui-survey-answer [props type label]
    (factory (prim/computed props {:type type :label label}))))

(defn answer-missing? [answer] (nil? (:survey-answer/value answer)))

(defsc SurveyQuestion
  [this {:keys [db/id survey-question/type
                survey-question/context survey-question/label
                survey-question/answer]}]
  {:query [:db/id :survey-question/type :survey-question/context :survey-question/label
           {:survey-question/answer (prim/get-query SurveyAnswer)}]
   :ident (fn [] [:survey-question/by-id id])}
  (s/ui-list-item nil
    (s/ui-segment #js {:raised true}
      (s/ui-header nil
        (s/ui-icon #js {:name "plug"})
        (s/ui-header-content nil
          "Project Costs"))
      (s/ui-container #js {:style #js {:padding "1em 1em"}}
        context
        (s/ui-divider nil)
        (s/ui-container #js {}
          (ui-survey-answer answer type label)
          (s/ui-button #js {:color "green" :disabled (answer-missing? answer)} "Next"))))))

(def ui-survey-question (prim/factory SurveyQuestion {:keyfn :db/id}))

(defsc Survey [this {:keys [:db/id :survey/questions ui/step] :as props}]
  {:query         [:ui/step :db/id {:survey/questions (prim/get-query SurveyQuestion)}]
   :ident         [:survey/by-id :db/id]
   :initial-state (fn [p]
                    {:db/id            1
                     :ui/step          0
                     :survey/questions [{:db/id                  1
                                         :survey-question/context
                                                                 "Experience shows that an minimally viable product launch can easily take 6 months of development time. Our up-front rate is negotiable but averages $30/hr. This means full-time development costs $5,160/month and your project launch will likely cost $30k or perhaps much more."
                                         :survey-question/label  "Is this manageable?"
                                         :survey-question/answer {:db/id               99
                                                                  :survey-answer/value nil}
                                         :survey-question/type   :boolean}]})}
  (dom/div nil
    (s/ui-header nil "Survey")
    (ui-survey-question (nth questions step))))

(def ui-survey (prim/factory Survey {:keyfn :db/id}))

(defsc SubmitProposal [this {:keys [screen-name ui/survey] :as props}]
  {:query         (fn [] [:screen-name {:ui/survey (prim/get-query Survey)}])
   :ident         (fn [] [screen-name :page])
   :initial-state (fn [params]
                    {:screen-name :submit-proposal
                     :ui/survey   (prim/get-initial-state Survey {})})}
  (dom/div nil
    (s/ui-segment #js {:style #js {:padding "2em 0em"} :vertical true}
      (s/ui-grid #js {:container true :stackable true :verticalAlign "middle"}
        (s/ui-grid-row #js {}
          (s/ui-grid-column #js {:width 16}
            (dom/h1 nil "Register and Tell Us More")
            (dom/p nil
              "We're excited to hear from you! We'd love to be able to help out every single client
              that has interest in our services. Here's how it works:")
            (s/ui-list #js {:bulleted true}
              (s/ui-list-item nil "Take a short survey to see if we're a match.")
              (s/ui-list-item nil "If that checks out, then register for an account!")
              (s/ui-list-item nil "The registration process will help you submit a non-disclosure agreement.")
              (s/ui-list-item nil "Once an NDA is executed we'll schedule a consultation to talk more."))
            (dom/p nil
              "Please start by completing this short survey.")))
        (s/ui-grid-row #js {}
          (s/ui-grid-column #js {:width 16}
            (ui-survey survey)
            ))))))
