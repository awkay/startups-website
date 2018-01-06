(ns startupsite.server
  (:require
    [fulcro.server :as core]
    [fulcro.easy-server :as easy]
    [com.stuartsierra.component :as component]
    [taoensso.timbre :as timbre]
    [ring.middleware.session :as session]
    [ring.middleware.session.store :as store]
    [ring.util.response :as response]
    [fulcro.util :as util]
    [clojure.string :as str]
    [clojure.java.io :as io]))

(defn top-html
  "Render the HTML for the SPA. There is only ever one kind of HTML to send, but the initial state and initial app view may vary.
  This function takes a normalized client database and a root UI class and generates that page."
  [normalized-client-state root-component-class]
  ; props are a "view" of the db. We use that to generate the view, but the initial state needs to be the entire db
  (let [;props                (db->tree (get-query root-component-class) normalized-client-state normalized-client-state)
        ;root-factory         (factory root-component-class)
        app-html             "SSR!" #_(dom/render-to-str (root-factory props))
        initial-state-script "" #_(ssr/initial-state->script-tag normalized-client-state)
        html                 (-> (io/resource "public/index.html")
                               slurp
                               (str/replace #"<!-- initial html -->" app-html)
                               (str/replace #"<!-- initial state -->" initial-state-script))]
    html))

(defn build-app-state
  "Builds an up-to-date app state based on the URL where the db will contain everything needed. Returns a normalized
  client app db."
  [user uri bidi-match language]
  #_(let [base-state       (ssr/build-initial-state (root/initial-app-state-tree) root/Root) ; start with a normalized db that includes all union branches. Uses client UI!
          normalized-state (cond-> base-state
                             :always (assoc :ui/ready? true))]
      normalized-state)
  {})

(defn render-page
  "Server-side render the entry page."
  [uri match user language]
  (let [normalized-app-state (build-app-state user uri match language)]
    (-> (top-html normalized-app-state nil)
      response/response
      (response/content-type "text/html"))))

(defn wrap-server-side-rendering
  "Ring middleware to handle all sends of the SPA page(s) via server-side rendering. If you want to see the client
  without SSR, just remove this component from the ring stack and supply an index.html in resources/public."
  [handler]
  (fn [req]
    (let [uri         (:uri req)
          user        nil
          bidi-match  nil                                   ; (bidi/match-route routing/app-routes uri) ; where they were trying to go. NOTE: This is shared code with the client!
          valid-page? (or (= "/" uri) (= "/index.html" uri)) ;(boolean bidi-match)
          language    (some-> req :headers (get "accept-language") (str/split #",") first keyword)]

      (timbre/info req)
      (timbre/info "Desired language " language)

      ; . no valid bidi match. BYPASS. We don't handle it.
      (if valid-page?
        (render-page uri bidi-match user language)
        (handler req)))))

(defrecord ServerSideRenderer [handler]
  component/Lifecycle
  (start [this]
    (let [vanilla-pipeline (easy/get-pre-hook handler)]
      (easy/set-pre-hook! handler (comp vanilla-pipeline
                                    (partial wrap-server-side-rendering))))
    this)
  (stop [this] this))

(defrecord RingSessions [handler session-store]
  component/Lifecycle
  (start [this]
    ; This is the basic pattern for composing into the existing pre-hook handler (which starts out as identity)
    ; If you're sure this is the only component hooking in, you could simply set it instead.
    (let [vanilla-pipeline (easy/get-pre-hook handler)]
      (easy/set-pre-hook! handler (comp vanilla-pipeline
                                    (fn [h] (session/wrap-session h {:store session-store})))))
    this)
  (stop [this] this))

(defrecord SessionStore [memory-store]
  store/SessionStore
  (read-session [_ key]
    (get @memory-store key))
  (write-session [_ key data]
    (let [key (or key (util/unique-key))]
      (swap! memory-store assoc key data)
      key))
  (delete-session [_ key]
    (swap! memory-store dissoc key)
    nil)
  component/Lifecycle
  (start [this] (assoc this :memory-store (atom {})))
  (stop [this] this))

(defn make-system [config-path]
  (easy/make-fulcro-server
    :config-path config-path
    :parser (core/fulcro-parser)
    :parser-injections #{:config :session-store}
    :components {:sessions      (component/using
                                  (map->RingSessions {})
                                  [:handler :session-store])
                 :session-store (map->SessionStore {})
                 :html5-routes  (component/using
                                  (map->ServerSideRenderer {})
                                  ; Technically, the server-side renderer does not use the session by direct code reference, but it needs it to go "first"
                                  [:handler :session-store])}))
