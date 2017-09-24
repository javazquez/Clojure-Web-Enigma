(ns web-enigma.db.core
  (:require
    [clj-time.jdbc]
    [conman.core :as conman]
    [mount.core :refer [defstate]]
    [web-enigma.config :refer [env]]))

(defstate ^:dynamic *db*
           :start (conman/connect! {:jdbc-url (env :database-url)})
           :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/queries.sql")

