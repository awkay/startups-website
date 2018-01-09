# Startups Website

This is the source for Fulcrologic's website related to consulting
for start-up companies.

## SSR

This is a full-stack app that uses SSR via Nashorn so it can also use
Semanic UI React. The configuration has a number of simple pieces:

- The index.html file has comments that are replaced by the server (state, html, and script tag).
- The SSR relies on an optimized js file for SSR, and a polyfill. The former must be built or SSR cannot work.
    - resources/externs.js is for adv. compile to ensure initial state variable is not renamed. See project.clj production cljs build.
- The SSR watches the timestamp of the generated .min.js file and updates nashorn if it changes.
- The root UI has the queries, and has to be cljc
- The client mount *must not run* in SSR mode (there is no DOM). See client_main.cljs. The polyfill sets a global that short-circuits the mount
- The nashorn_rendering.cljs exports a `server-render` function that the Nashorn script engine can eval (with UI props) to render a string of DOM
- See nashorn_rendering.clj for the code that manages the Nashorn instance and rendering
- There is no mutable state, so it seems safe to call the render in Nashorn from multiple threads. The data is passed in as args.
- I get 100ms render times once it is up and running...not as good as clj factory methods running on the JVM, but not bad.

## Running in Development Mode

This project embeds the control of the API web server into the Figwheel system and *adds in automatic hot code reload*
 for the server as well as the client. At the moment you need to run:

1. The normal -Ddev figwheel config
2. An auto-build of the "ssr" cljs build
3. (optional) An nrepl client connecting to 7888 if you want a CLJ REPL

Thus, you're really running with a single VM as far as the server and figwheel are concerned but the production cljs
build is running in a separate window.

A command line version of all of this would be:

In terminal A:

```
JVM_OPTS=-Ddev lein run -m clojure.main script/figwheel.clj
```

In terminal B:

```
lein with-profile production cljsbuild auto ssr
```

Then use http://localhost:3000

The nREPL is on port 7888

Compiler errors for both client and server will appear in the heads-up display in the browser!

## LICENSE

Copyright 2017, Fulcrologic, LLC
All Rights Reserved
