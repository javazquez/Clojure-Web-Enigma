(ns ^:figwheel-no-load web-enigma.app
  (:require [web-enigma.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
