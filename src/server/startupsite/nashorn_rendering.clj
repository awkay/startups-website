(ns startupsite.nashorn-rendering
  (:require [clojure.java.io :as io]
            [fulcro.client.util :as cutil]
            [taoensso.timbre :as timbre])
  (:import (javax.script ScriptEngineManager ScriptException SimpleScriptContext ScriptContext Compilable SimpleBindings)
           (jdk.nashorn.api.scripting NashornScriptEngine)
           (java.io File)))

(def production-js "public/js/startupsite.min.js")

(defonce nashorn (atom nil))
(defn build-engine [] (-> (ScriptEngineManager.) (.getEngineByName "nashorn")))
(defn- read-js [path] (io/reader (io/resource path)))

(defn- start-nashorn
  "Start a single Nashorn instance capable of running *one* SSR at a time. Not thread safe.

  Checks the timestamp of the js-file. If the file has changed, then it restarts nashorn to ensure
  the rendering is correct."
  ([]
   (start-nashorn false))
  ([reinit]
   (when (or (nil? @nashorn) reinit)
     (timbre/info "Initializing Nashorn")
     (reset! nashorn (build-engine)))))

(defonce last-compile-time (atom 0))

(defn- stale? [file]
  (let [^File f (io/as-file file)]
    (and (.exists f) (> (.lastModified f) @last-compile-time))))

(defn load-and-eval-js []
  (locking last-compile-time
    (when (stale? (io/resource production-js))
      (let [start (System/currentTimeMillis)
            script-engine ^NashornScriptEngine @nashorn
            _ (.eval script-engine (read-js "public/nashorn-polyfill.js"))
            _ (.eval script-engine (read-js production-js))
            end (System/currentTimeMillis)]
        (reset! last-compile-time end)
        (timbre/info "JS Eval took (ms): " (- end start))))))

(defn ^String nashorn-render
  "Render the given tree of props via Nashorn. Returns the HTML as a string."
  [props]
  (try
    (locking nashorn (start-nashorn))
    (load-and-eval-js)
    (let [start (System/currentTimeMillis)
          string-props (cutil/transit-clj->str props)
          script-engine ^NashornScriptEngine @nashorn
          namespc (.eval script-engine "startupsite.nashorn_rendering")
          result (.invokeMethod script-engine namespc "server_render" (into-array [string-props]))
          html (String/valueOf result)
          end (System/currentTimeMillis)]
      (timbre/info "Rendering time (ms): " (- end start))
      html)
    (catch ScriptException e
      (timbre/debug "Server-side render failed. This is an expected error when not running from a production build with adv optimizations.")
      (timbre/error "Rendering exception:" e))))

