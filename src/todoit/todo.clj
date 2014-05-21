(ns todoit.todo
  (:require [io.pedestal.interceptor :refer [defhandler]]
            [io.pedestal.http.route :refer [url-for]]
            [ring.util.response :refer [response redirect]]
            [datomic.api :as d]
            [todoit.todo.db :as db]
            [todoit.todo.view :as view]))

(defhandler create [req]
  (let [title (get-in req [:form-params "title"])
        description (get-in req [:form-params "description"])]
    (when title
      (print title)
      (db/create-todo title description))
    (redirect (url-for :todos))))

(defhandler index [req]
  (let [todos (db/all-todos (d/db db/conn))]
    (response (view/todo-index todos))))
