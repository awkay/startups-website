(ns startupsite.client
  (:require [fulcro.client :as fc]
            [startupsite.ui.html5-routing :as routing]
            [fulcro.server-render :as ssr]
            [fulcro.client.primitives :as prim]))

(defonce app (atom
               (fc/new-fulcro-client
                 :initial-state (when-let [v (ssr/get-SSR-initial-state)] (atom v))
                 :started-callback (fn [{:keys [reconciler] :as app}]
                                     (let [state (prim/app-state reconciler)
                                           root  (prim/app-root reconciler)
                                           {:keys [ui/ready?]} @state]
                                       (when ready?
                                         (routing/start-routing root)))))))
