(ns startupsite.nashorn-rendering
  (:require [fulcro.client.primitives :as prim]
            [startupsite.ui.root :as root]
            [fulcro.client :as fc]
            cljsjs.react.dom.server
            [fulcro.client.logging :as log]
            [fulcro.client.util :as util]))

(def ui-root (prim/factory root/Root))

(defn ^:export server-render [props-str]
  (if-let [props (some-> props-str util/transit-str->clj)]
    (js/ReactDOMServer.renderToString (ui-root props))
    (js/ReactDOMServer.renderToString (ui-root (prim/get-initial-state root/Root nil)))))
