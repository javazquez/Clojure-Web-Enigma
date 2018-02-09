# Clojure Web Enigma

generated using Luminus version "2.9.11.89"

This app was created as a introduction to clojure programming and should not be used in production. The Twitter integration was only written with the use case of one person ever using the application so YMMV. If you get an error sending a tweet, click the "reset token" link to login to twitter again.

In order to use the Twitter functionality, you can add a map to your profiles.clj file in the root directory that environ can find. see the twitter.clj file for example.  

{:profiles/dev  {:env {:database-url "jdbc:h2:./web_enigma_dev.db"
                       :app-consumer-key "<enter key here>"
                       :app-consumer-secret "<enter secret here>"}
             
## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein run 


To start figwheel, run:

    lein figwheel

## Docker 

    docker pull javazquez/clojure-web-enigma

## List of resources consulted

- http://enigma.louisedade.co.uk/enigma.html
- Enigma Machine Mechanism (feat. a 'Double Step')  https://www.youtube.com/watch?v=hcVhQeZ5gI4
- http://users.telenet.be/d.rijmenants/en/enigmatech.htm#reflector
- https://www.scienceabc.com/innovation/the-imitation-game-how-did-the-enigma-machine-work.html
- https://en.wikipedia.org/wiki/Enigma_machine
- https://en.wikipedia.org/wiki/Cryptanalysis_of_the_Enigma
- https://en.wikipedia.org/wiki/Enigma_rotor_details
- http://practicalcryptography.com/ciphers/enigma-cipher/






