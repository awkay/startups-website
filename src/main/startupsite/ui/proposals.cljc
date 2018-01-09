(ns startupsite.ui.proposals
  (:require
    [fulcro.client.dom :as dom]
    [fulcrologic.semantic-ui.factories :as s]
    [fulcrologic.semantic-ui.icons :as i]
    [fulcro.client.mutations :as m :refer [defmutation]]
    [fulcro.client.data-fetch :as df]
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

(defmulti render-question-input (fn [component id type params] type))

(defmethod render-question-input :boolean [this id type {:keys [value label]}]
  (s/ui-form nil
    (s/ui-form-group #js {:grouped true}
      (dom/label nil label)
      (s/ui-form-field #js {:inline true}
        (dom/input #js {:name     id
                        :onChange (fn [] (prim/transact! this `[(answer-question {:id ~id :value true})]))
                        :checked  (identical? value true) :type "radio"})
        (dom/label nil "Yes"))
      (s/ui-form-field #js {:inline true}
        (dom/input #js {:name     id
                        :onChange (fn [] (prim/transact! this `[(answer-question {:id ~id :value false})]))
                        :checked  (identical? value false) :type "radio"})
        (dom/label nil "No")))))

(defmethod render-question-input :multi-select [this id type {:keys [label value params] :or {value #{}}}]
  (s/ui-form nil
    (s/ui-form-group #js {:grouped true}
      (dom/label nil label)
      (map-indexed
        (fn [idx opt]
          (s/ui-form-field #js {:inline true :key (str "checkbox-" idx)}
            (dom/input #js {:name     id
                            :onChange (fn []
                                        (let [v                (:option/value opt)
                                              already-present? (contains? value (:option/value opt))
                                              checked-value    (if already-present?
                                                                 (disj value v)
                                                                 (conj (or value #{}) v))]
                                          (prim/transact! this `[(answer-question {:id ~id :value ~checked-value})])))
                            :checked  (contains? value (:option/value opt))
                            :type     "checkbox"})
            (dom/label nil (:option/label opt)))) params))))

(defmethod render-question-input :radio-select [this id type {:keys [label value params]}]
  (s/ui-form nil
    (s/ui-form-group #js {:grouped true}
      (dom/label nil label)
      (map-indexed
        (fn [idx {l :option/label v :option/value}]
          (s/ui-form-field #js {:inline true :key (str "radio-" idx)}
            (dom/input #js {:name     id
                            :onChange (fn [] (prim/transact! this `[(answer-question {:id ~id :value ~v})]))
                            :checked  (= value v)
                            :type     "radio"})
            (dom/label nil l))) params))))

(defsc SurveyAnswer [this {:keys [db/id survey-answer/value] :as props} {:keys [type label params]}]
  {:query         [:db/id :survey-answer/value]
   :ident         [:survey-answer/by-id :db/id]
   :initial-state {:db/id :param/id}}
  (render-question-input this id type {:value value :label label :params params}))

(let [factory (prim/factory SurveyAnswer {:keyfn :db/id})]
  (defn ui-survey-answer [props params]
    (factory (prim/computed props params))))

(defn answer-missing? [answer] (nil? (:survey-answer/value answer)))

(defsc SurveyQuestion
  [this {:keys [db/id survey-question/type
                survey-question/icon
                survey-question/context
                survey-question/label
                survey-question/params
                survey-question/title
                survey-question/answer]} {:keys [onNext onPrevious onSubmit step last-step]}]
  {:query [:db/id
           :survey-question/type
           :survey-question/icon
           :survey-question/context
           :survey-question/title
           :survey-question/label
           :survey-question/params
           {:survey-question/answer (prim/get-query SurveyAnswer)}]
   :ident (fn [] [:survey-question/by-id id])}
  (let [last-step? (= last-step step)]
    (s/ui-list-item nil
      (s/ui-segment #js {:raised true}
        (s/ui-header nil
          (s/ui-icon #js {:name (or icon "plug")})
          (s/ui-header-content nil title))
        (s/ui-container #js {:style #js {:padding "1em 1em"}}
          context
          (s/ui-divider nil)
          (s/ui-container #js {}
            (ui-survey-answer answer {:type type :label label :params params})
            (s/ui-button #js {:color    "green"
                              :onClick  #(when onPrevious (onPrevious))
                              :disabled (= 0 step)} "Back")
            (s/ui-button #js {:color    "green"
                              :onClick  (if last-step?
                                          #(when onSubmit (onSubmit))
                                          #(when onNext (onNext)))
                              :disabled (answer-missing? answer)} (if last-step? "Submit" "Next"))))))))

(def ui-survey-question (prim/factory SurveyQuestion {:keyfn :db/id}))

(defmutation next-question [{:keys [survey-id]}]
  (action [{:keys [state]}]
    (let [step-path [:survey/by-id survey-id :ui/step]]
      (swap! state update-in step-path inc))))

(defmutation prior-question [{:keys [survey-id]}]
  (action [{:keys [state]}]
    (let [step-path [:survey/by-id survey-id :ui/step]]
      (swap! state update-in step-path dec))))

(defsc Survey [this {:keys [:db/id :survey/questions ui/step] :as props}]
  {:query         [:ui/step :db/id {:survey/questions (prim/get-query SurveyQuestion)}]
   :ident         [:survey/by-id :db/id]
   :initial-state {}}
  (let [next-question  (fn [] (prim/transact! this `[(next-question {:survey-id ~id})]))
        prior-question (fn [] (prim/transact! this `[(prior-question {:survey-id ~id})]))
        finish         (fn [])
        last-step      (dec (count questions))
        question       (when (pos? last-step) (nth questions step))]
    (when question
      (dom/div nil
        (s/ui-header nil "Survey")
        (s/ui-button #js {:onClick #(df/load this :query/proposal-survey Survey {:marker false
                                                                                 :target [:submit-proposal :page :ui/survey]})} "Reload Survey")
        (ui-survey-question (prim/computed question {:onPrevious prior-question
                                                     :step       step
                                                     :last-step  last-step
                                                     :onSubmit   finish
                                                     :onNext     next-question}))))))

(def ui-survey (prim/factory Survey {:keyfn :db/id}))

(defn survey-missing? [state-map] (not (some-> state-map :survey/by-id seq)))

(defmutation ensure-survey-loaded [params]
  (action [{:keys [state] :as env}]
    (when (survey-missing? @state)
      (df/load-action env :query/proposal-survey Survey {:marker false
                                                         :target [:submit-proposal :page :ui/survey]})))
  (remote [{:keys [state] :as env}]
    (when (survey-missing? @state)
      (df/remote-load env))))

(defsc SubmitProposal [this {:keys [screen-name ui/survey] :as props}]
  {:query             (fn [] [:screen-name {:ui/survey (prim/get-query Survey)}])
   :componentDidMount (fn [] (prim/transact! this `[(ensure-survey-loaded {})]))
   :ident             (fn [] [screen-name :page])
   :initial-state     (fn [params]
                        {:screen-name :submit-proposal})}
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
            (ui-survey survey)))))))

