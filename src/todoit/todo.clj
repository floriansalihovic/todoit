(ns todoit.todo
  (:require [io.pedestal.interceptor :refer [defhandler]]
            [io.pedestal.http.route :refer [url-for]]
            [ring.util.response :refer [response redirect]]
            [datomic.api :as d]
            [todoit.todo.db :as db]))

(defhandler index [req]
  (let [todos (db/all-todos (d/db db/conn))]
    (response (str "<html>"
                     "<body>"
                       "<div>"
                       (if (seq todos)
                         (mapv :todo/title todos)
                         "<p>All done here!</p>")
                        "<form method=\"POST\" action=\"/todos\">"
                          "<input name=\"title\" placeholder=\"title\">"
                          "<input name=\"description\" placeholder=\"description\">"
                          "<input type=\"submit\">"
                        "</form>" 
                       "</div>"
                     "</body>"
                   "</html>"))))
