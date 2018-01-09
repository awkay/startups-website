(ns startupsite.api.read
  (:require
    [fulcro.server :refer [defquery-entity defquery-root]]
    [taoensso.timbre :as timbre]
    [fulcro.client.primitives :as prim]))

(defquery-root :query/proposal-survey
  (value [env params]
    {:db/id            1
     :ui/step          0
     :survey/questions [{:db/id                  1
                         :survey-question/title  "Costs"
                         :survey-question/context
                                                 "Experience shows that an minimally viable product launch can easily take 6 months of development time.
                                                 Our up-front rate is negotiable but averages $30/hr. This means full-time development costs $5k/month
                                                 and getting to initial project launch will likely cost more than $30k."
                         :survey-question/label  "Do you have a plan on how to cover these initial costs?"
                         :survey-question/icon   "money"
                         :survey-question/answer {:db/id               (prim/tempid)
                                                  :survey-answer/value nil}
                         :survey-question/type   :boolean}
                        {:db/id                  2
                         :survey-question/title  "Collateral"
                         :survey-question/context
                                                 (str
                                                   "Our business model is to share risks with you by allowing you to pay a low rate "
                                                   "for our services initially, but you will owe the balance of our negotiated "
                                                   "full rate for all hours worked. We secure this debt by holding the intellectual property "
                                                   "rights on the software until you have paid for it fully. "
                                                   "If you decide you no longer want to pay then you lose the right to use the software.")
                         :survey-question/label  "I'm OK with using the software rights as collateral for unpaid debt to Fulcrologic, LLC."
                         :survey-question/icon   "copyright"
                         :survey-question/answer {:db/id               (prim/tempid)
                                                  :survey-answer/value nil}
                         :survey-question/type   :boolean}
                        {:db/id                  3
                         :survey-question/title  "Things to Build"
                         :survey-question/context
                                                 (str
                                                   "In order for us to meet your needs we will have to allocate the correct resources "
                                                   "to your project we need to know what kind of thing(s) you need built.")
                         :survey-question/label  "Please select the kinds of things you need help with:"
                         :survey-question/icon   "computer"
                         :survey-question/answer {:db/id               (prim/tempid)
                                                  :survey-answer/value nil}
                         :survey-question/params [{:option/label "Company Website" :option/value :website}
                                                  {:option/label "Web Application (Software as a Service)" :option/value :saas}
                                                  {:option/label "Apple iPhone App" :option/value :iphone}
                                                  {:option/label "Android App" :option/value :android}
                                                  {:option/label "Embedded Software (e.g. software on custom hardware)" :option/value :embedded}]
                         :survey-question/type   :multi-select}
                        {:db/id                   4
                         :survey-question/title   "Business Maturity"
                         :survey-question/context "We'd like to know what stage of business development you've reached."
                         :survey-question/label   "Please select the item on this list that best describes your status."
                         :survey-question/icon    "level up"
                         :survey-question/answer  {:db/id               (prim/tempid)
                                                   :survey-answer/value nil}
                         :survey-question/params  [{:option/label "I/We have an idea I'd like to explore." :option/value :idea}
                                                   {:option/label "I/We have a business plan." :option/value :plan}
                                                   {:option/label "I/We have relationships with potential paying customers" :option/value :customers}
                                                   {:option/label "The company has existing revenue." :option/value :revenue}
                                                   {:option/label "The company is already profitable." :option/value :profitable}]
                         :survey-question/type    :radio-select}]}))