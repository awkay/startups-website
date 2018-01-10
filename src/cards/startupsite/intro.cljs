(ns startupsite.intro
  (:require [devcards.core :as dc :refer-macros [defcard]]
            [startupsite.ui.components :as comp]
            [om.dom :as dom]))

(defcard SVGPlaceholder
  "# SVG Placeholder"
  (comp/ui-placeholder {:w 200 :h 200}))
