(ns web-enigma.db
  (:require [web-enigma.enigma :as enigma ]))

;;default 
(def e-machine
  (enigma/enigma-machine {:reflector enigma/reflector-b
                          :left-rotor enigma/rotor1
                          :middle-rotor enigma/rotor2
                          :right-rotor enigma/rotor3
                          :settings "AAA"
                          :plugboard {}
                          }))
(def default-db
  {:page :home
   :enigma-machine e-machine
   :encoded-message ""
   :ui-settings "AAA"
   :user-message {:error false
                  :message  ""}
   })
