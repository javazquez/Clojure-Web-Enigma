(ns web-enigma.twitter
 (:require [oauth.client :as oauth]
           [clj-http.client :as client]
           [environ.core :refer [env]]))


;; Create a Consumer, in this case one to access Twitter.
;; Register an application at Twitter (https://dev.twitter.com/apps/new)
;; to obtain a Consumer token and token secret.


(def consumer (oauth/make-consumer (env :app-consumer-key) 
                                   (env :app-consumer-secret)
                                   "https://api.twitter.com/oauth/request_token"
                                   "https://api.twitter.com/oauth/access_token"
                                   "https://api.twitter.com/oauth/authorize"
                                   :hmac-sha1))

(defn refresh-token
  ""
  [req-token]
  (oauth/refresh-token consumer req-token))

;; Fetch a request ;TODO: oken that a OAuth User may authorize
;; 
;; If you are using OAuth with a desktop application, a callback URI
;; is not required. 
(defn request-token []
  (oauth/request-token consumer (env :callback-url)))

;; Send the User to this URI for authorization, they will be able 
;; to choose the level of access to grant the application and will
;; then be redirected to the callback URI provided with the
;; request-token.
(defn approval-uri [req-token]
  (oauth/user-approval-uri consumer 
                           (:oauth_token req-token)))

;; Assuming the User has approved the request token, trade it for an access token.
;; The access token will then be used when accessing protected resources for the User.
;;
;; If the OAuth Service Provider provides a verifier, it should be included in the
;; request for the access token.  See [Section 6.2.3](http://oauth.net/core/1.0a#rfc.section.6.2.3) of the OAuth specification
;; for more information.
(defn create-access-token-response 
  [req-token verifier] 
  (oauth/access-token consumer 
                      req-token
                      verifier))

;; Each request to a protected resource must be signed individually.  The
;; credentials are returned as a map of all OAuth parameters that must be
;; included with the request as either query parameters or in an
;; Authorization HTTP header.
(defn create-tweet
  [message]
  {:status message})

(defn credentials [access-token-response tweet-msg]
  (oauth/credentials consumer
                     (:oauth_token access-token-response)
                     (:oauth_token_secret access-token-response)
                     :POST
                     "https://api.twitter.com/1.1/statuses/update.json"
                     (create-tweet tweet-msg)))

;; Post with clj-http... message is a tweet Message
(defn post-tweet [creds tweet-message]  
  (client/post "https://api.twitter.com/1.1/statuses/update.json" 
             {:cookie-policy :standard
              :query-params (merge creds
                                   (create-tweet tweet-message))}))
