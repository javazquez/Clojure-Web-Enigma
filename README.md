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







