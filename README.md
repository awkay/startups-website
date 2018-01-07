# Startups Website

This is the source for Fulcrologic's website related to consulting
for start-up companies.

## SSR

This is a full-stack app that uses SSR via Nashorn so it can also use
Semanic UI React. The configuration has a number of simple pieces:

- The index.html file has comments that are replaced by the server.
- The SSR relies on an optimized js file for SSR, and a polyfill
- The initial state relies on externs.js
- The root UI has the queries, and has to be cljc, even though render
need not work (can just be `#?(:cljs ...`)

## Running in Development Mode

You need *both* cljs builds going (production and dev). The server
will render the initial index with the production build (which is cached, so
you'll have to restart the server to see a change in initial SSR). The
server should be run with `-Ddev` which causes it to embed the correct
*figwheel dev* build into the HTML (instead of a pointer to the production
one).

Thus, in dev mode the SSR is rendered by an optimized build, but the
hot code reload will work because the js that the *browser* actually loads
is the development one.


## LICENSE

Copyright 2017, Fulcrologic, LLC
All Rights Reserved
