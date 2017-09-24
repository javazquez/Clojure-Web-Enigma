(ns user
  (:require [mount.core :as mount]
            [web-enigma.figwheel :refer [start-fw stop-fw cljs]]
            web-enigma.core))

(defn start []
  (mount/start-without #'web-enigma.core/repl-server))

(defn stop []
  (mount/stop-except #'web-enigma.core/repl-server))

(defn restart []
  (stop)
  (start))


