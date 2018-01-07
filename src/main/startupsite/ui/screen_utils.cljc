(ns startupsite.ui.screen-utils)

#?(:clj (def clj->js identity))

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

