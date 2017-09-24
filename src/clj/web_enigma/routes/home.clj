(ns web-enigma.routes.home
  (:require [web-enigma.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [web-enigma.twitter :as twitter]))

(defn home-page []
  (layout/render "home.html"))

(def request-token (atom ""))
(def access-token (atom ""))
(def twitter-auth (atom {:oauth_token ""
                         :oauth_verifier ""}))

(defn post-to-twitter
  [oauth_verifier  message]
  (let [access-token-response  (twitter/create-access-token-response 
                            @request-token
                            oauth_verifier)
        creds (twitter/credentials
               access-token-response
               message)]
    (twitter/post-tweet creds message)
    (reset! access-token access-token-response)))

(defn get-token
  []
  (reset! request-token (twitter/request-token ))
  (twitter/approval-uri @request-token))

(defroutes home-routes
  (GET "/" [oauth_verifier oauth_token ] 
       (reset! twitter-auth {:oauth_token oauth_token
                             :oauth_verifier oauth_verifier})
       (home-page))

  (GET "/reset_token" []
       (reset! request-token (twitter/request-token))
       (home-page))
  
  (POST "/tweet" [ message]
        (if (empty? @access-token)
          (response/bad-request "Please sign into twitter by clicking the reset token link above and then clicking sign into twitter. Sorry, but hey, this is a demo app :D") ;(post-to-twitter (:oauth_verifier @twitter-auth) message)
          (do (-> (twitter/credentials @access-token
                                       message)
                  (twitter/post-tweet message))
              (response/no-content))))

  (GET "/twitter_auth" []
       (response/found (get-token)))

  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
           (response/header "Content-Type" "text/plain; charset=utf-8"))))


