(ns web-enigma.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [web-enigma.core-test]))

(doo-tests 'web-enigma.core-test)

