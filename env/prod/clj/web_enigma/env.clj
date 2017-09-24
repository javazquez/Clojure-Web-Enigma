(ns web-enigma.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[web-enigma started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[web-enigma has shut down successfully]=-"))
   :middleware identity})
