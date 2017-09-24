(ns web-enigma.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [web-enigma.ajax :refer [load-interceptors!]]
            [web-enigma.events]
            [cemerick.url :as url]
            [web-enigma.enigma :as enigma]
            [cljsjs.clipboard :as clippy])
  (:import goog.History))
;below snippet from https://github.com/cljsjs/packages/tree/master/clipboard
(defn- clipboard-button [label target]
  (let [clipboard-atom (atom nil)]
    (r/create-class
     {:display-name "clipboard-button"
      :component-did-mount
      #(let [clipboard (new js/Clipboard (r/dom-node %))]
         (reset! clipboard-atom clipboard))
      :component-will-unmount
      #(when-not (nil? @clipboard-atom)
         (.destroy @clipboard-atom)
         (reset! clipboard-atom nil))
      :reagent-render
      (fn []
        [:button.clipboard
         {:data-clipboard-target target
          :type "button"
          :class "btn btn-info"
          :on-click #(rf/dispatch [:update-user-message 
                                   #js {:error false
                                        :message "Copied!"}])}
         label])})))

(defn- handler [response]
  (rf/dispatch [:update-user-message #js {:error false
                                          :message (str "Tweet Sent " response)}]))

(defn- error-handler [{:keys [status status-text]:as error}]
  (rf/dispatch [:update-user-message #js {:error true
                                          :message (str "Error sending Tweet -> " 
                                                        " "
                                                        (:response error))}]))

(defn- nav-link [uri title page collapsed?]
  (let [selected-page (rf/subscribe [:page])]
    [:li.nav-item
     {:class (when (= page @selected-page) "active")}
     [:a.nav-link
      {:href uri
       :on-click #(reset! collapsed? true)} title]]))

(defn- navbar []
  (r/with-let [collapsed? (r/atom true)]
    [:nav.navbar.navbar-dark.bg-primary
     [:button.navbar-toggler.hidden-sm-up
      {:on-click #(swap! collapsed? not)} "☰"]
     [:div.collapse.navbar-toggleable-xs
      (when-not @collapsed? {:class "in"})
      [:a.navbar-brand {:href "#/"} "Web-Enigma-Demo"]
      [:ul.nav.navbar-nav
       [nav-link "#/" "Home" :home collapsed?]
       [nav-link "/reset_token" "Reset Token" :reset_token collapsed?]]]]))

(defn- about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:img {:src (str js/context "/img/warning_clojure.png")}]]]])

(defn- get-rotor-values []
  {:right-rotor (.-value (js/document.getElementById "right-rotor"))
   :left-rotor (.-value (js/document.getElementById "left-rotor"))
   :middle-rotor (.-value (js/document.getElementById "middle-rotor"))})

(defn- rotor-select-input
  [{:keys [name id default-value change-fn] :or { default-value "", change-fn identity} }]
  [:select {:name name 
            :id id
            :class "form-control"
            :default-value default-value
            :on-change change-fn }
   [:option "I"]
   [:option "II"]
   [:option "III"]])

(defn- rotor-input
  [{:keys [rotor-name default-val label-text]}]
  [:div {:class " col-md-2"}
   [:label {:for rotor-name 
            :class "col-md-1 col-form-label col-form-label-lg" }
   label-text]
   [:div (rotor-select-input {:name rotor-name
                              :id rotor-name
                              :default-value default-val
                              :change-fn #(rf/dispatch [:rotor-update  (get-rotor-values)])})] ])
(defn- internal-rotor-display 
  []
  [:div {:class "form-group row"}
   [:div {:class  "col-md-1 text-muted "}
    "Interal Rotor settings   "
    @(rf/subscribe [:ui-settings])]])

(defn- message-input 
  []
  [:div {:class "form-group row"}
   [:textarea {:name "message"
               :class "form-control col" 
               :rows 3
               :maxLength 137
               :placeholder "type message"
               :on-change #(rf/dispatch [:encode-message 
                                         (if-not (= 0 (-> % .-target .-value .trim count))
                                           (-> % .-target .-value))])}]
   (if-not (get (:query 
                 (url/url 
                  (-> js/window .-location .-href))) 
                "oauth_token")
      
     [:a {:class "btn btn-primary"
          :href "/twitter_auth"} "Twitter Sign in"]
     [:button {:type "button"
               :on-click #(POST "/tweet" 
                                {:params {:message @(rf/subscribe [:encoded-message])}
                                 :handler handler
                                 :error-handler error-handler })
               :class "btn btn-success"} "Tweet message"])
   (if-not (or (nil?  @(rf/subscribe [:encoded-message])) ; ugly hack to hide if no messages.. refactor in the future
               (= ""  @(rf/subscribe [:encoded-message])))
     [clipboard-button "Copy" "#encoded-message"])] )

(defn- rotor-settings-input
  []
  [:div {:class "col-md-2"}
   [:label {:for "start-position" 
            :class "col-md-1 col-form-label col-form-label-lg" }
    "Ring settings "]
   [:div 
    [:input {:class "form-control"
             :name "start-position" 
             :id "start-position"
             :type "text"
             :maxLength 3
             :pattern "[A-Za-z]{3}"
             :on-blur (fn [e]                           
                        (if (= 3  (count  (-> e .-target .-value)))
                          (do  (rf/dispatch [:update-ui-settings 
                                             (-> e .-target .-value .toUpperCase)])
                               (rf/dispatch [:update-settings (-> e .-target .-value .toUpperCase)]))))
             :default-value "AAA"}]]])

(defn- encoded-message-display
  []
  [:div {:class "container"}
   [:div {:class "form-group row  " } 
    [:h6  {:class " text-muted " }  "Encoded Message"  ]
    [:p  {:id "encoded-message" :class " text-muted "} @(rf/subscribe [:encoded-message])]]
   
   [:div {:class "form-group row"}
    (if-not (or (nil? (:message  @(rf/subscribe [:user-message]))) ; ugly hack to hide if no messages.. refactor in the future
                (= "" (:message @(rf/subscribe [:user-message]))))
      [:p{:class (str  "alert " (if (:error @(rf/subscribe [:user-message])) 
                                  "alert-danger"
                                  "alert-info" )) } (:message @(rf/subscribe [:user-message])) ])]
   (message-input)])

(def footer    
  [:div {:class "container"} 
   [:div {:class "row"} 
    [:div {:class "col"} [:span  "Created by Juan Vazquez"]]
    [:div {:class "col"} [:a {:href "http://javazquez.com/juan/"} " Blog "]] 
    [:div {:class "col"} [:a {:href "https://github.com/javazquez/"} " Github "]] 
    [:div {:class "col"} [:a {:href "https://twitter.com/javazquez"} " Twitter"]]]])

(defn- reflector-input
  []
  [:div  {:class "col-md-2"}
   [:label {:for "reflector" 
            :class "col-md-2 col-form-label col-form-label-lg" }
    "Reflector Options"]
   [:select {:name "reflector" 
             :class "form-control"
             :default-value "B" 
             :on-change #(rf/dispatch [:reflector-update (-> % .-target .-value)])}
    [:option {:name "reflector-a"} "A"]
    [:option {:name "reflector-b"} "B"]
    [:option {:name "reflector-c"} "C"]]])

(defn- enigma-component [ ]
  [:form
   [:div {:class "form-group row"}
    (reflector-input)
    (rotor-input {:rotor-name "left-rotor" 
                  :default-val "I"
                  :label-text "Left Rotor"})
    (rotor-input {:rotor-name "middle-rotor" 
                  :default-val "II"
                  :label-text "Middle Rotor"})
    (rotor-input {:rotor-name "right-rotor" 
                  :default-val "III"
                  :label-text "Right Rotor"})
    (rotor-settings-input)]
   (internal-rotor-display)
   (encoded-message-display)
   footer])

(defn home-page []
  [:div.container
   (enigma-component ) ])

(def pages
  {:home #'home-page
   :about #'about-page})

(defn page []
  [:div
   [navbar]
   [(pages @(rf/subscribe [:page]))]])

;; -------------------------
;; Routes 
(secretary/set-config! :prefix "#")
;{:query-params {:oauth_token "_1CInwAAAAAA2VrAAAABXpLre90", :oauth_verifier "yYJ2DBbNC4Dcbp1W5PUEZETQlb7QSy1g"}}
(secretary/defroute "/" {:as params}
  (rf/dispatch [:set-active-page :home]))

(secretary/defroute "/about" []
  (rf/dispatch [:set-active-page :about]))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  (hook-browser-navigation!)
  (mount-components))
