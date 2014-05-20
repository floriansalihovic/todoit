(ns todoit.core
  (:require [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.http.route :as route :refer [router]]
            [io.pedestal.http :as http]
            [ns-tracker.core :refer [ns-tracker]]
            [ring.handler.dump :refer [handle-dump]]
            [io.pedestal.interceptor :refer [defon-request]]
            [todoit.todo :as todo]
            [io.pedestal.http.body-params :refer [body-params]]))

(defon-request capitalize-name [req]
  (update-in req [:query-params :name]
    (fn [name] (when name (clojure.string/capitalize name)))))

(defn hello-world [req]
  (let [name (get-in req [:query-params :name])]
    {:status 200
     :body (str "Hello, " (if (not(clojure.string/blank? name)) name "world") "!")
     :headers {}}))

(defn goodbye-world [req]
  {:status 200
   :body "Goodbye, cruel world."
   :headers {}})

(defroutes routes
 [[["/" ^:interceptors [http/html-body]
    ["/hello" ^:interceptors [capitalize-name] {:get hello-world}]
    ["/goodbye" {:get goodbye-world}]
    ["/request" {:any handle-dump}]
    ["/todos" {:get [:todos todo/index]
               :post [:todos#create todo/create]}]]]])

(def modified-namespaces (ns-tracker "src"))

(def service
  {::http/interceptors [http/log-request
                        http/not-found
                        route/query-params
                        (body-params)
                        (router (fn []
                          (doseq [ns-sym (modified-namespaces)]
                            (require ns-sym :reload))
                            routes))]
   ::http/port 8080})

(defn -main [& args]
  (-> service
      http/create-server
      http/start))
