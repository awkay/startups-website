(ns startupsite.card-utils
  (:require [fulcro.ui.elements :as ele]
            [fulcro.client.dom :as dom]))

(defn semantic-ui-frame
  "Wrap an example in an iframe so we can load external Semantic UI CSS without affecting the containing page."
  [width height & children]
  (ele/ui-iframe {:frameBorder 1 :height height :width width}
    (apply dom/div #js {:key "example-frame-key"}
      (dom/style nil ".boxed {border: 1px solid black}")
      (dom/link #js {:rel "stylesheet" :href "css/semantic-2.1.12.min.css"})
      children)))
