(ns startupsite.ui.components
  (:require
    [fulcro.client.primitives :as prim :refer [defsc]]
    #?(:cljs [goog.object :as gobj])
    [fulcro.client.dom :as dom]
    [fulcro.client.logging :as log]
    [fulcro.client.mutations :as m :refer [defmutation]]))

(defsc PlaceholderImage [this {:keys [w h label]}]
  (let [label (or label (str w "x" h))]
    (dom/svg #js {:width w :height h}
      (dom/rect #js {:width w :height h :style #js {:fill        "rgb(200,200,200)"
                                                    :strokeWidth 2
                                                    :stroke      "black"}})
      (dom/text #js {:textAnchor "middle" :x (/ w 2) :y (/ h 2)} label))))

(def ui-placeholder (prim/factory PlaceholderImage))

(defn ease-out-quad [time-elapsed start target-change duration]
  (let [t  (/ time-elapsed (/ duration 2))
        tm (dec t)]
    (if (< t 1)
      (+ start (* (/ target-change 2) t t))
      (-> (/ (- target-change) 2)
        (* (dec (* tm (- tm 2))))
        (+ start)))))

(defn request-animation-frame [f] #?(:cljs (if js/requestAnimationFrame (js/requestAnimationFrame f) (js/setTimeout f 16))))
(defn current-time-ms [] #?(:cljs (.getTime (new js/Date))))

(defn scroll-to* [element to duration callback]
  #?(:cljs
     (let [current-pos     (fn [] (.-scrollTop element))
           start           (current-pos)
           change          (- to start)
           animation-start (current-time-ms)
           end-time        (+ animation-start duration)
           animating       (atom true)
           lastpos         (atom nil)
           update-scroll   (fn [val]
                             (reset! lastpos val)
                             (set! (.-scrollTop element) val))
           animate-scroll  (fn contiue-animation []
                             (when @animating
                               (request-animation-frame contiue-animation)
                               (let [now        (current-time-ms)
                                     elapsed-ms (- now animation-start)
                                     val        (js/Math.floor (ease-out-quad elapsed-ms start change duration))]
                                 (if @lastpos
                                   (if (identical? @lastpos (current-pos))
                                     (update-scroll val)
                                     (reset! animating false))
                                   (update-scroll val))
                                 (when (> now end-time)
                                   (update-scroll to)
                                   (reset! animating false)
                                   (when callback (callback))))))]
       (request-animation-frame animate-scroll))))

(defn get-scroll-target-dom-node
  "Given the app state and scroll target ID, returns the DOM node for that scroll target (if present)."
  [state target-id]
  (some-> state
    (get-in [:scroll-target/by-id target-id :scroll-target/element])
    meta
    :dom-node))

(defn element-offset [body element]
  #?(:cljs
     (let [bodyRect (.getBoundingClientRect body)
           elemRect (.getBoundingClientRect element)
           offset   (- (.-top elemRect) (.-top bodyRect))]
       offset)))

(defmutation scroll-to
  "Mutation: Scroll the page so that the given ScrollTarget (by id) is at the top of the page."
  [{:keys [target duration-ms onScrolled] :or {duration-ms 300}}]
  (action [{:keys [state]}]
    #?(:clj nil
       :cljs
            (when-let [dom-node (get-scroll-target-dom-node @state target)]
              (let [body          (or (.-documentElement js/document) (.-body js/document))
                    target-offset (element-offset body dom-node)]
                (scroll-to* body target-offset duration-ms onScrolled))))))

(defsc ScrollTarget
  "A scroll target for animated page scroll. Stores the dom element as metadata in app state."
  [this {:keys [:db/id] :as props}]
  {:query         [:db/id :scroll-target/element]
   :ident         [:scroll-target/by-id :db/id]
   :initial-state {:db/id :param/id :scroll-target/element {}}}
  #?(:cljs (dom/div #js {
                         :ref (fn [r] (when r
                                        (m/set-value! this :scroll-target/element (with-meta {} {:dom-node r}))))} "")))

(def ui-scroll-target (prim/factory ScrollTarget {:keyfn :db/id}))