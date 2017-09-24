(ns web-enigma.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [web-enigma.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[web-enigma started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[web-enigma has shut down successfully]=-"))
   :middleware wrap-dev})
