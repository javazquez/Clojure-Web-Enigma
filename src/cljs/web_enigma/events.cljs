(ns web-enigma.events
  (:require [web-enigma.db :as db]
            [re-frame.core :refer [dispatch reg-event-db reg-sub]]
            [web-enigma.enigma :as enigma]
            [clojure.string :as str ]))



;;dispatchers
(def enigma-resolver
  {:A enigma/reflector-a
   :B enigma/reflector-b
   :C enigma/reflector-c
   :I enigma/rotor1
   :II enigma/rotor2
   :III enigma/rotor3 })

(reg-event-db
 :initialize-db
 (fn [_ _]
   db/default-db))

(reg-event-db
 :set-active-page
 (fn [db [_ page]]
   (assoc db :page page)))

(reg-event-db
  :rotor-update
  (fn [db [_ rotor-map]]
    (try
      (let [new-db (assoc db
                   :enigma-machine
                   (enigma/enigma-machine 
                    (merge (:enigma-machine db) 
                           (reduce-kv #(assoc %1 
                                              %2 
                                              (get enigma-resolver (keyword %3)))
                                      {} rotor-map))))]
        (dispatch [:update-user-message ""])
        new-db)
      (catch js/Error e
        (dispatch [:update-user-message e ])
        (assoc  db :enigma-machine (:enigma-machine db))))))

(reg-event-db
  :reflector-update
  (fn [db [_ reflector]]
    (assoc-in db
              [:enigma-machine :reflector]
              (get enigma-resolver (keyword reflector)))))

(reg-event-db
  :update-settings
  (fn [db [_ settings]]
    (try
          (assoc db
           :enigma-machine
           (enigma/enigma-machine (merge (:enigma-machine db) 
                                         {:settings settings})))
      (catch js/Error e
          (dispatch [:update-user-message e ])
        db )))) 

(reg-event-db
  :update-ui-settings
  (fn [db [_ settings]]
    (assoc db
           :ui-settings
           settings)))

(reg-event-db
  :update-user-message
  (fn [db [_ msg]]
    (assoc db
              :user-message 
              {:message (aget msg "message")
               :error  (aget msg "error")}
           )));;pull message out of js error

;need to take settings out of new enigma machine to update ui
(reg-event-db
 :encode-message
 (fn [db [_ message]]
   (try
     (->> (if-not (empty? message )     
            (let [e-machine (enigma/encode-string
                             (:enigma-machine db) 
                             (.toUpperCase  (->>  (str/split message #"\s+" ) 
                                                  (str/join "" ))) )]
              (dispatch [:update-ui-settings (-> e-machine :enigma-machine :settings)])
              (dispatch [:update-user-message "" ])
              (:result e-machine))
            (do (dispatch [:update-user-message "" ])
                ""))
          (assoc 
           db 
           :encoded-message ))
     (catch js/Error e
            (dispatch [:update-user-message e ])
            db))))

;;subscriptions
(reg-sub
 :encoded-message
 (fn [db _]
   (apply str 
          (map #(reduce str %) 
               (interpose " " 
                          (partition 5 5 [" "] (:encoded-message db)))))))

(reg-sub
 :updated-settings
 (fn [db _]
  (get-in db [:enigma-machine :settings])))

(reg-sub
  :ui-settings
  (fn [db _]
    (:ui-settings db)))

(reg-sub
  :twitter-auth
  (fn [db _]
    (:twitter-auth db )))

(reg-sub
  :page
  (fn [db _]
    (:page db)))


(reg-sub
  :user-message
  (fn [db _]
    (:user-message db)))
