# todoit

A CRUD application based on Ryan Neufeld's Pedastal workshop.

## Project setup

Creating a project based on the standard leiningen template.

    lein new todoit

Then the dependencies are added.

    [org.clojure/clojure "1.6.0"]
    [io.pedastal/pedastal.service "0.3.0-SNAPSHOT"]
    [io.pedastal/pedastal.service-tools "0.3.0-SNAPSHOT"]
    [io.pedastal/pedastal.jetty "0.3.0-SNAPSHOT"]

```pedestal.service``` is the base dependency of a Pedastal web app. ```pedastal.service-tools``` provides logging functions used
in this application. ```pedastal.jetty``` is an adapter for the Jetty server.
Next, a ```:main``` declaration is added, pointing to the default namespace provided by the standard lein template.

    :main todoit.core

Starting with a default handler in ```src/todoit/core.clj```, which is created similarly to handlers in ring, it takes a request and returns a map, containg at least 3 elements.

    (ns todoit.core)

    (defn hello-world [req]
      {:status 200
       :body "Hello, world!"
       :headers {}})


To use the handler, routes have to be defined. This is done by modifying ```src/todoit/core.clj```, requiring ```io.pedastal.http.route.definition``` and refering to the function ```defroutes```.

    (ns todoit.core
      (:require [io.pedastal.http.route.definition :refer [defroutes]]))

    (defn hello-world [req]
      {:status 200
       :body "Hello, world!"
       :headers {}})

```defroutes``` is a short hand for all the internal routes kept by the system. This allows to add routes, which will be used to invoke server side functionality.

    (ns todoit.core
      (:require [io.pedastal.http.route.definition :refer [defroutes]]))

    (defn hello-world [req]
      {:status 200
       :body "Hello, world!"
       :headers {}})
    
    (defroutes routes
      [[["/"
         ["/hello" {:get hello-world}]]]])

At the smallest level, a list is created containg a list of routes and an associated handler map. The http verbs used will determine which handler is being invoked. The routes can have children as well, which follow the handler map. The root path ```/``` for example is a parent of the child route ```/hello```. The nesting of lists is a result of Pedestals possibility to host multiple applications. The implicit context could be replaced by an explicit, using an application's name, port or host. An example could be the hosting of a public facing website and an api hosted.

    [[:public "exampel.com"
      ;; routes ...]
     [:api "api.example.com"
      ;; routes ...]]

The next task to be solved is providing a service, which exposes the routes to the world. Starting with adding a namespace providing the functionality needed.
         
    (ns todoit.core
      (:require [io.pedastal.http.route.definition :refer [defroutes]]
                [io.pedastal.http.route as route :refer [router]]
                [io.pedastal.http :as http]))

    (defn hello-world [req]
      {:status 200
       :body "Hello, world!"
       :headers {}})
    
    (defroutes routes
      [[["/"
         ["/hello" {:get hello-world}]]]])

The ```router``` function will be used to construct an interceptor, whereas ```io.pedastal.http``` will provide a lot of functions used by the app. The service will be defined as a simple object providing a map. The map will contain namespaced keywords Pedastal understands and knows how to work with.
         
    (ns todoit.core
      (:require [io.pedastal.http.route.definition :refer [defroutes]]
                [io.pedastal.http.route as route :refer [router]]
                [io.pedastal.http :as http]))
    
    (defn hello-world [req]
      {:status 200
       :body "Hello, world!"
       :headers {}})
    
    (defroutes routes
      [[["/"
         ["/hello" {:get hello-world}]]]])
    
    (def service {::http/interceptors [(router routes)]
                  ::http/port 8080})

It contains a list of interceptors, which basically a router constructed from the routes handling the incoming request and mappting them to functions and a port to handle the requests at.

The final thing to be added is a main function corresponding to the one already added to the project definition.

         
    (ns todoit.core
      (:require [io.pedastal.http.route.definition :refer [defroutes]]
                [io.pedastal.http.route as route :refer [router]]
                [io.pedastal.http :as http]))
    
    (defn hello-world [req]
      {:status 200
       :body "Hello, world!"
       :headers {}})
    
    (defroutes routes
      [[["/"
         ["/hello" {:get hello-world}]]]])
    
    (def service {::http/interceptors [(router routes)]
                  ::http/port 8080})
                  
    (defn -main [& args]
      (-> service
          http/create-server
          http/start))

The ```-main``` function takes an arbitrary amount of arguments which will be ignored for now and using Clojure's threading macro taking the service, creates a server from it and starts the server created. With the main function in place, running the server should be possible. Invoking

    lein run

Should start the server and lets us see ```Hello, world!``` in the browser when ```localhost:8080/hello``` is requested. To reduce the output created by the Jetty to the minimum and yet necessary data, in ```/resources``` a file called ```logback.xml``` gets created.

    <?xml version="1.0" encoding="UTF-8"?>
    <!-- Simple logback configuration for STOUT-only. -->
    <configuration scan="true" scanPeriod="10 seconds">
    
	  <appender name="STOUT" class="ch.qos.logback.core.ConsoleAppender">
	    
	    <!-- encoder defaults to ch.qos.logback.classic.encoder.PatternLayoutEncoder. -->
	    <encoder>
		  <pattern>%-5level %logger{36} - %msg%n</pattern>
	    </encoder>
	    
	    <!-- Only log level INFO and above. -->
	    <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
	      <level>INFO</level>
	    </filter>
	  </appender>
    
      <root level="INFO">
	    <appender-ref ref="STOUT"/>
      </root>
    
      <!-- For loggers in these namespaces, log all levels. -->
      <logger name="user" level="ALL" />
      <logger name="io.pedestal" level="ALL" />
      <logger name="datomic.db" level="ERROR" />
    
    </configuration>

To have a more continuous workflow, a library called ```ns-tracker``` is added. It provides an  observation mechanism for files and when these change. This is added as a dependency into ```project.clj```.

    (defproject todoit "0.1.0-SNAPSHOT"
      :description "FIXME: write description"
      :url "http://example.com/FIXME"
      :license {:name "Eclipse Public License"
                :url "http://www.eclipse.org/legal/epl-v10.html"}
      :dependencies [[org.clojure/clojure "1.6.0"]
                     [io.pedestal/pedestal.service "0.3.0-SNAPSHOT"]
                     [io.pedestal/pedestal.service-tools "0.3.0-SNAPSHOT"]
                     [io.pedestal/pedestal.jetty "0.3.0-SNAPSHOT"]
                     [ns-tracker "0.2.2"]
                     ]
      :main todoit.core)

This library will be used in ```core.todoit``` while refering to the function ```ns-tracker```.

    (ns todoit.core
      (:require [io.pedastal.http.route.definition :refer [defroutes]]
                [io.pedastal.http.route as route :refer [router]]
                [io.pedastal.http :as http]
                [ns-tracker.core :refer [ns-tracker]]))
    
    (defn hello-world [req]
      {:status 200
       :body "Hello, world!"
       :headers {}})
    
    (defroutes routes
      [[["/"
         ["/hello" {:get hello-world}]]]])
    
    (def service {::http/interceptors [(router routes)]
                  ::http/port 8080})
                  
    (defn -main [& args]
      (-> service
          http/create-server
          http/start))

The function will be used in a ```def modified-namespaces``` to observe the ```src``` directory, monitoring it for changes.

    (ns todoit.core
      (:require [io.pedastal.http.route.definition :refer [defroutes]]
                [io.pedastal.http.route as route :refer [router]]
                [io.pedastal.http :as http]
                [ns-tracker.core :refer [ns-tracker]]))
    
    (defn hello-world [req]
      {:status 200
       :body "Hello, world!"
       :headers {}})
    
    (defroutes routes
      [[["/"
         ["/hello" {:get hello-world}]]]])
    
    (def modified-namespaces (ns-tracker "src"))
    
    (def service {::http/interceptors [(router routes)]
                  ::http/port 8080})
                  
    (defn -main [& args]
      (-> service
          http/create-server
          http/start))

The neat thing about the router function is, that it does not just take a predefiend map, but also a function. This is a handy observation, because substituting it with a function allows us to dynamically load routes. The anonymous function will iterate over all known namespaces to reload them if necessary returning the modified routes table.

    (ns todoit.core
      (:require [io.pedastal.http.route.definition :refer [defroutes]]
                [io.pedastal.http.route as route :refer [router]]
                [io.pedastal.http :as http]
                [ns-tracker.core :refer [ns-tracker]]))
    
    (defn hello-world [req]
      {:status 200
       :body "Hello, world!"
       :headers {}})
    
    (defroutes routes
      [[["/"
         ["/hello" {:get hello-world}]]]])
    
    (def modified-namespaces (ns-tracker "src"))
    
    (def service {::http/interceptors [(router (fn []
        (doseq [ns-sym (modified-namespaces)]
          (require ns-sym :reload))
          routes))]
                  ::http/port 8080})
                  
    (defn -main [& args]
      (-> service
          http/create-server
          http/start))

To check if it is working, a new route ```/goodbye``` is added while the server is running. 


    (ns todoit.core
      (:require [io.pedastal.http.route.definition :refer [defroutes]]
                [io.pedastal.http.route as route :refer [router]]
                [io.pedastal.http :as http]
                [ns-tracker.core :refer [ns-tracker]]))
    
    (defn hello-world [req]
      {:status 200
       :body "Hello, world!"
       :headers {}})
    
    (defn goodbye-world [req]
      {:status 200
       :body "Goodbye, cruel world!"
       :headers {}})
    
    (defroutes routes
      [[["/"
         ["/hello" {:get hello-world}]
         ["/goodbye" {:get goodbye-world}]]]])
    
    (def modified-namespaces (ns-tracker "src"))
    
    (def service {::http/interceptors [(router (fn []
        (doseq [ns-sym (modified-namespaces)]
          (require ns-sym :reload))
          routes))]
                  ::http/port 8080})
                  
    (defn -main [& args]
      (-> service
          http/create-server
          http/start))

One will notice though, that the server does not log any useful information to the console. That is because routes and interceptors stand for their own. What's basically missing is a chain of middleware to aggregate the functions and handle errors, logging etc. This is basically done in a self contained context for each request and response.

TODO: revisit minute 23:00

Interceptors can have an ```:enter``` function, which manipulates the incoming by adding new keys and corresponding values. A ```:leave``` function can be provided optionally as well. This may be very handy when a response is about to be send back and the content-type of the response may be modified for example. Finally, an optional :error function can be implemented. It will catch the error and let it bubble up by the interceptor chain and let it invoke an interceptor, which is an error responder. What gets returned from that function will become a new response.

One thing to be noticed is, that routers are interceptors as well. Interceptor can be used at the service level or at various points thoughout the routing table. For example interceptors can be attached to all of a routes children.

    [[["/" ^:intercptors [...]
      ["/hello" {:get hello-world}]
      ["/goodbye" {:get goodbye-world}]
     ]]]

Incpetors can be added deeper in the routes table as well.

    [[["/" ^:intercptors [...]
      *["/hello" *{:get *hello-world}]
      ["/goodbye" {:get goodbye-world}]
     ]]]

The ```*``` indicates an attachment point.

Getting back to the application, two interceptors will be added. One handling requests to unknown destinations and one for request logging. (27:39)

The log function is actually provided by ```io.pedestal.http```. Two interceptors will be used. ```http/log-request``` and ```http/not-found```. ```http/not-found``` is a loead interceptor. This means that after no response will be returned by any route or interceptor, it will create a standard ```404``` error. Calling [/hello](http://localhost:8080/hello),  [/goodbye](http://localhost:8080/goodbye) and  [/goodbye-world](http://localhost:8080/goodbye-world) will display the proper log messages and messages in the browser.

By adding a new route ```/request``` the anatomy of a request can by introspected and and displayed visually. It will be available for any http verb (```GET```, ```POST```, ```PUT```, and ```DELETE```). The route will invoke a handler ```request``` which will in its response display the ```req``` argument map. This route will be addressed from the command line at this point, because it would the resonse would be downloaded as a file from the browser. Hitting

    curl localhost:8080/request

In the command line will prompt a big junk of data, which is basically a map. Since this data might be of big interest for monitoring the application's state or bug fixing, a handler will be used to print the map into a web page. It is borrowed from Ring, another Clojure web framework. Ring's handlers are drop in compatible, which makes a reuse of existing functionality very easy. This is done by adding a dependency in ```project.clj``` for the Ring library.

    (defproject todoit "0.1.0-SNAPSHOT"
      :description "FIXME: write description"
      :url "http://example.com/FIXME"
      :license {:name "Eclipse Public License"
                :url "http://www.eclipse.org/legal/epl-v10.html"}
      :dependencies [[org.clojure/clojure "1.6.0"]
                     [io.pedestal/pedestal.service "0.3.0-SNAPSHOT"]
                     [io.pedestal/pedestal.service-tools "0.3.0-SNAPSHOT"]
                     [io.pedestal/pedestal.jetty "0.3.0-SNAPSHOT"]
                     [ns-tracker "0.2.2"]
                     [ring/ring-devel "1.2.2"]
                     ]
      :main todoit.core)

After restarting the server and pulling the Ring dependency, adding Ring as a dependency to ```src/todoit/core.clj```, using the dump handler to create a web page displaying all data necessary.

In ```src/todoit/core.clj``` the namespace ```ring.handle.dump``` is added, using the ```handle-dump``` function to create a website. The ```request``` handler can be delete, since the route will use ```handle-dump```.

    (ns todoit.core
      (:require [io.pedestal.http.route.definition :refer [defroutes]]
                [io.pedestal.http.route :as route :refer [router]]
                [io.pedestal.http :as http]
                [ns-tracker.core :refer [ns-tracker]]
                [ring.handler.dump :refer [handle-dump]]))
    
    (defn hello-world [reg]
      {:status 200
       :body "Hello, world!"
       :headers {}})
    
    (defn goodbye-world [req]
      {:status 200
       :body "Goodbye, cruel world."
       :headers {}})
    
    (defroutes routes
     [[["/"
        ["/hello" {:get hello-world}]
        ["/goodbye" {:get goodbye-world}]
        ["/request" {:any handle-dump}]]]])
    
    (def modified-namespaces (ns-tracker "src"))
    
    (def service
      {::http/interceptors [http/log-request
                            http/not-found
                            (router (fn []
                              (doseq [ns-sym (modified-namespaces)]
                                (require ns-sym :reload))
                                routes))]
       ::http/port 8080})
    
    (defn -main [& args]
      (-> service
          http/create-server
          http/start))

Having that in place, [/request](http://localhost:8080/request) will return page containg all data provided. 

The small application being build should provide a little more functionality, like printing the name of a user passed in the query string. Requesting [/request?name=ryan](http://localhost:8080/request?name=ryan) will print the appropriate queury string. This is actually not that useful, since an arbitray amount of arguments could be passed that way and working with a map of query arguments would be much more easier to handle. This is a behaviour which should be applied to any incoming request.

The first action to take is adding the ```route/query-param``` to the list of interceptors. This transforms the query passed into a map of key/value pairs.

    (ns todoit.core
      (:require [io.pedestal.http.route.definition :refer [defroutes]]
                [io.pedestal.http.route :as route :refer [router]]
                [io.pedestal.http :as http]
                [ns-tracker.core :refer [ns-tracker]]
                [ring.handler.dump :refer [handle-dump]]))
    
    (defn hello-world [reg]
      {:status 200
       :body "Hello, world!"
       :headers {}})
    
    (defn goodbye-world [req]
      {:status 200
       :body "Goodbye, cruel world."
       :headers {}})
    
    (defroutes routes
     [[["/"
        ["/hello" {:get hello-world}]
        ["/goodbye" {:get goodbye-world}]
        ["/request" {:any handle-dump}]]]])
    
    (def modified-namespaces (ns-tracker "src"))
    
    (def service
      {::http/interceptors [http/log-request
                            http/not-found
                            route/query-param
                            (router (fn []
                              (doseq [ns-sym (modified-namespaces)]
                                (require ns-sym :reload))
                                routes))]
       ::http/port 8080})
    
    (defn -main [& args]
      (-> service
          http/create-server
          http/start))

Refreshing [/request?name=ryan](http://localhost:8080/request?name=ryan) will now actually display ```:params``` and ```:query-params``` as a map. Since this operation is done on the context's request map, getting single arguments like ```name``` used in our example is a piece of pie. This allows for a more sphisticated ```hello-world``` handler. The handler will be modified to print the name provided by the query.

    (ns todoit.core
      (:require [io.pedestal.http.route.definition :refer [defroutes]]
                [io.pedestal.http.route :as route :refer [router]]
                [io.pedestal.http :as http]
                [ns-tracker.core :refer [ns-tracker]]
                [ring.handler.dump :refer [handle-dump]]))
    
    (defn hello-world [reg]
      (let [name (get-in req [:query-params :name])]
        {:status 200
         :body (str "Hello, " name "!")
         :headers {}}))
    
    (defn goodbye-world [req]
      {:status 200
       :body "Goodbye, cruel world."
       :headers {}})
    
    (defroutes routes
     [[["/"
        ["/hello" {:get hello-world}]
        ["/goodbye" {:get goodbye-world}]
        ["/request" {:any handle-dump}]]]])
    
    (def modified-namespaces (ns-tracker "src"))
    
    (def service
      {::http/interceptors [http/log-request
                            http/not-found
                            route/query-param
                            (router (fn []
                              (doseq [ns-sym (modified-namespaces)]
                                (require ns-sym :reload))
                                routes))]
       ::http/port 8080})
    
    (defn -main [& args]
      (-> service
          http/create-server
          http/start))

This will print the name passed URL query string, but it will take the name as is, when the name is written lowercase, it will be printed as such. To fix this wrong behavior, a new interceptor will be added. This is done by adding the interceptor namespace ```io.pedestal.interceptors```. It provides a couple of helpers, an the one which will be referred to for capitalizing the name is ```defon-request```. The interceptor defined will respind only to the enter phase of a request. The interceptor being implemented will use CLojure's ```update-in``` function to capitalize the name if it is in the query parameters. Since the interceptor is most interesting in conjunction with the ```/hello``` route, it will be added to a list of interceptors specifically for this route.

    (ns todoit.core
      (:require [io.pedestal.http.route.definition :refer [defroutes]]
                [io.pedestal.http.route :as route :refer [router]]
                [io.pedestal.http :as http]
                [ns-tracker.core :refer [ns-tracker]]
                [ring.handler.dump :refer [handle-dump]]
                [io.pedestal.interceptor :refer [defon-request]]))
    
    (defon-request capitalize-name [req]
      (update-in req [:query-params :name]
        (fn [name] (when name (clojure.string/capitalize name)))))
    
    (defn hello-world [req]
      (let [name (get-in req [:query-params :name])]
        {:status 200
         :body (str "Hello, " name "!")
         :headers {}}))
    
    (defn goodbye-world [req]
      {:status 200
       :body "Goodbye, cruel world."
       :headers {}})
    
    (defroutes routes
     [[["/"
        ["/hello" ^:interceptors [capitalize-name] {:get hello-world}]
        ["/goodbye" {:get goodbye-world}]
        ["/request" {:any handle-dump}]]]])
    
    (def modified-namespaces (ns-tracker "src"))
    
    (def service
      {::http/interceptors [http/log-request
                            http/not-found
                            route/query-params
                            (router (fn []
                              (doseq [ns-sym (modified-namespaces)]
                                (require ns-sym :reload))
                                routes))]
       ::http/port 8080})
    
    (defn -main [& args]
      (-> service
          http/create-server
          http/start))
 
This will print the name passed properly.

## Persistence
 
The next big piece of work will be adding persistence to the project. Starting by adding a new file. In ```resources``` a file named ```todos.edn``` should be added. It contains a small schema for the datomic database.
 
    [{:db/id #db/id [:db.part/db]
      :db/ident :todo/title
      :db/valueType :db.type/string
      :db/cardinality :db.cardinality/one
      :db/doc "The title of the todo."
      :db.install/_attribute :db.part/db} 
     {:db/id #db/id [:db.part/db]
      :db/ident :todo/description
      :db/valueType :db.type/string
      :db/cardinality :db.cardinality/one
      :db/doc "The description of the todo."
      :db.install/_attribute :db.part/db} 
     {:db/id #db/id [:db.part/db]
      :db/ident :todo/completed?
      :db/valueType :db.type/boolean
      :db/cardinality :db.cardinality/one
      :db/doc "The completion status of the todo."
      :db.install/_attribute :db.part/db}]

It contains basically a list of maps describing a todos attributes with attributes. The attributes used are ```title``` and ```description``` of type string and a ```completion?``` indicator of type boolean. In ```project.clj``` datomic will be added as a dependency.

    (defproject todoit "0.1.0-SNAPSHOT"
      :description "FIXME: write description"
      :url "http://example.com/FIXME"
      :license {:name "Eclipse Public License"
                :url "http://www.eclipse.org/legal/epl-v10.html"}
      :dependencies [[org.clojure/clojure "1.6.0"]
                     [io.pedestal/pedestal.service "0.3.0-SNAPSHOT"]
                     [io.pedestal/pedestal.service-tools "0.3.0-SNAPSHOT"]
                     [io.pedestal/pedestal.jetty "0.3.0-SNAPSHOT"]
                     [ns-tracker "0.2.2"]
                     [ring/ring-devel "1.2.2"]
                     [com.datomic/datomic-free "0.9.4699"
                      :exclusions [org.slf4j/jul-to-slf4j
                                   org.slf4j/slf4j-nop]]
                     ]
      :main todoit.core)

To interact with the application and playing around with the persistence layer, a repl session will be started.

    lein repl

This will download the dependencies and connect to the application.

Now, ```src/todoit``` a new folder ```todo``` will be added with the file ```db.clj```. In the file evrything about the model will be described.

    (ns todoit.todo.db
      (:require [datomic.api :as d]))
    
    ; generating a unique database uri for an in-memory database.
    (defonce uri (str "datomic:mem://" (gensym "todos")))
    ; creating a database for the uri.
    (d/create-database uri)
    ; defining a connection to it.
    (def conn (d/connect uri))
    
    ; schema-transaction
    (def schema-tx (->> "todos.edn"
                        clojure.java.io/resource
                        slurp
                        (clojure.edn/read-string {:readers *data-readers*})))
    
    ; d/transact is basically a database commit which returns a future
    ; it is dereferenced with the @ symbol
    @(d/transact conn schema-tx)

This basically the starting point. It creates a database at the created uri and commits our schema to the database.

To actually add some data, a create-todo function will be added, which takes a title and a description and commits the data to the database.

    (ns todoit.todo.db
      (:require [datomic.api :as d]))
    
    ; generating a unique database uri for an in-memory database.
    (defonce uri (str "datomic:mem://" (gensym "todos")))
    ; creating a database for the uri.
    (d/create-database uri)
    ; defining a connection to it.
    (def conn (d/connect uri))
    
    ; schema-transaction
    (def schema-tx (->> "todos.edn"
                        clojure.java.io/resource
                        slurp
                        (clojure.edn/read-string {:readers *data-readers*})))
    
    ; d/transact is basically a database commit which returns a future
    ; it is dereferenced with the @ symbol
    @(d/transact conn schema-tx)
    
    (defn todo-tx [title description]
      (cond-> {:db/id (d/tempid :db.part/user)
               :todo/title title
               :todo/description description
               :todo/completed? false}
            description (assoc :todo/description description)  ;; add description if not nil
            true vector))                                      ;; always wrap anything in a vector,
                                                               ;; because d/transact always expects
                                                               ;; a list of entities.
    
    (defn create-todo [title description]
      @(d/transact conn (todo-tx title description)))


> TODO: remove comments in code paraphrase them

This can now be tested in the repl.

    todoit.core=> (require 'todoit.todo.db)

When no compile errors occourred, the function ```in-ns``` can be used to set the scope to the given namespace providing the possibility to operate on functions from within the namespace.

    todoit.core=> (in-ns 'todoit.todo.db)

For a first interaction with the database, todo-tx is called with only a title and no description.

    todoit.todo.db=> (todo-tx "Get it done." nil)

This will return a vector of entities.

    todoit.todo.db=> (todo-tx "Get it done." "Because I have to.")

Having a ```create-todo``` function already in place, that can be used as well. It will return, in contrast to ```todo-tx```, more information about the database state including the previous state of the data.

    todoit.todo.db=> (require 'todoit.todo.db :reload)
    todoit.todo.db=> (create-todo "Get it done." "Because I have to.")

Querying all todos can be done similar to querying in SQL databases.

    (ns todoit.todo.db
      (:require [datomic.api :as d]))
    
    (defonce uri (str "datomic:mem://" (gensym "todos")))
    (d/create-database uri)
    (def conn (d/connect uri))
    
    (def schema-tx (->> "todos.edn"
                        clojure.java.io/resource
                        slurp
                        (clojure.edn/read-string {:readers *data-readers*})))

    @(d/transact conn schema-tx)
    
    (defn todo-tx [title description]
      (cond-> {:db/id (d/tempid :db.part/user)
                :todo/title title
                :todo/completed? false}
              description (assoc :todo/description description)
              true vector))

    (defn create-todo [title description]
      @(d/transact conn (todo-tx title description)))

    (defn all-todos [db]
      (->> (d/q '[:find ?id
                  :where [?id :todo/title]] db)
           (map first)
           (map #(d/entity db %))))

The function ```all-todos``` provides access to all todos created. The not so intuitive result of the query is being processed, to get a lazily constructed map back. The query returns a set of vectors containing an id. ```(map first)``` is applied to the set exatracting the ids and ```(map #(d/entity db %))))``` will construct an entity. It is constructed lazily through, containing at first only the id. When more properties are required, those will be requested from the database.

To see the title of the first todo loaded, the following expression will print the value.

    todoit.todo.db=> (:todo/title (first (all-todos (d/db conn))))

To get access to all of an entities attribute d/touch comes in handy - which should be avoided in production because it involves a lot of read access.

    todoit.todo.db=> (map d/touch (all-todos (d/db conn)))

Another function which is a requirement for the todo list we're building is a function to update the status of a todo. Instead of passing a whole todo, the id and the new status will be passed.

    (ns todoit.todo.db
      (:require [datomic.api :as d]))
    
    (defonce uri (str "datomic:mem://" (gensym "todos")))
    (d/create-database uri)
    (def conn (d/connect uri))
    
    (def schema-tx (->> "todos.edn"
                        clojure.java.io/resource
                        slurp
                        (clojure.edn/read-string {:readers *data-readers*})))
    
    @(d/transact conn schema-tx)
    
    (defn todo-tx [title description]
      (cond-> {:db/id (d/tempid :db.part/user)
                :todo/title title
                :todo/completed? false}
            description (assoc :todo/description description)
            true vector))
    
    (defn create-todo [title description]
      @(d/transact conn (todo-tx title description)))
    
    (defn all-todos [db]
      (->> (d/q '[:find ?id
                  :where [?id :todo/title]] db)
           (map first)
           (map #(d/entity db %))))
    
    (defn toggle-status [id status]
        @(d/transact conn [[:db/add id :todo/completed? status]]))

This can be tested easily by reloading the namespace and then invoking the method by a valid id. The id can be copy pasted from todos created.

    todoit.todo.db=> (require 'todoit.todo.db :reload)
    todoit.todo.db=> (toggle-status 17592186045418 true)

The last thing to add is a function which provides the possibility to delete a todo.

    (ns todoit.todo.db
      (:require [datomic.api :as d]))

    (defonce uri (str "datomic:mem://" (gensym "todos")))
    (d/create-database uri)
    (def conn (d/connect uri))
    
    (def schema-tx (->> "todos.edn"
                        clojure.java.io/resource
                        slurp
                        (clojure.edn/read-string {:readers *data-readers*})))
    
    @(d/transact conn schema-tx)
    
    (defn todo-tx [title description]
      (cond-> {:db/id (d/tempid :db.part/user)
                :todo/title title
                :todo/completed? false}
            description (assoc :todo/description description)
            true vector))
    
    (defn create-todo [title description]
      @(d/transact conn (todo-tx title description)))
    
    (defn all-todos [db]
      (->> (d/q '[:find ?id
                  :where [?id :todo/title]] db)
           (map first)
           (map #(d/entity db %))))
    
    (defn toggle-status [id status]
      @(d/transact conn [[:db/add id :todo/completed? status]]))
    
    (defn delete-todo [id]
      @(d/transact conn [[:db.fn/retractEntity id]]))

To delete a todo, it is only necessary to pass the id of the todo. `:db.fn/retractEntity` will take care of removing the entity. But deleting is the wrong verb to use. The database will act as if all facts about the entity are deleted from this this point in time forward. That is a unique characteristic of Datomic.

> TODO: Write a query completed-todos that ensures [?id :todo/completed? true].

## Wiring up C & R (create and read of crud)

1. controller actions for index and creating TODOs
2. Wire up those actions to routes
3. Learn how to name routes for easier reference
4. Building a basic Web page.
5. Learn about interceptors which help to parse a forms

This chapter will be all about wiring up CRUD operations to routes. Although we working in th repl is fun, this is server side development now. `lein run` restarts the server.

Starting by adding a controller, which is stored in `src/todoit/todo.clj`.

    (ns todoit.todo
      (:require [io.pedestal.interceptor :refer [defhandler]]
                [io.pedestal.http.route :refer [url-for]]
                [ring.util.response :refer [response redirect]]
                [datomic.api :as d]
                [todoit.todo.db :as db]))

There are already some familiar requirements, new is the usage of pedestal's `defhandler` macro. It basically constructs a request to response handler and is an indicator for an interceptor function, rather a simple processing instruction.
`url-for` is used for contructing urls for responses. From Ring the functions are imported to have some less typing.

Starting with a plain text index function to display a list of todos.

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
                           "</div>"
                         "</body>"
                       "</html>"))))

The index handler basically reads all todos from the database and maps the todos to their title and prints them in the resulting page. The handler will be used in `src/todoit/core.clj` as a handler for a route.

    (ns todoit.core
      (:require [io.pedestal.http.route.definition :refer [defroutes]]
                [io.pedestal.http.route :as route :refer [router]]
                [io.pedestal.http :as http]
                [ns-tracker.core :refer [ns-tracker]]
                [ring.handler.dump :refer [handle-dump]]
                [io.pedestal.interceptor :refer [defon-request]]
                [todoit.todo :as todo]))
    
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
     [[["/"
        ["/hello" ^:interceptors [capitalize-name] {:get hello-world}]
        ["/goodbye" {:get goodbye-world}]
        ["/request" {:any handle-dump}]
        ["/todos" {:get todo/index}]]]])
    
    (def modified-namespaces (ns-tracker "src"))
    
    (def service
      {::http/interceptors [http/log-request
                            http/not-found
                            route/query-params
                            (router (fn []
                              (doseq [ns-sym (modified-namespaces)]
                                (require ns-sym :reload))
                                routes))]
       ::http/port 8080})
    
    (defn -main [& args]
      (-> service
          http/create-server
          http/start))

When [/todos](http://localhost:8080/todos) is opened in the browser, a plain text message should appear, since no proper content type is set. When working with a web based application serving mostly html, the usage of an interceptor post-processing the response so that it will by displayed properly in the browser. The interceptor `io.pedestal.http/html-body` is a fitting match. The interceptor will be used at the root route, so that it will be applied to all routes respectively. When the browser is now refreshed, the html is displayed properly.

> TODO: no restart required?

What's currently missing is at least a simple way to add a new todo. This will be changed by added a form to the controller's index handler's response.

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

To inspect the data being submitted, a handler for `/todos` will be added in the routes table for `:post`, invoking `handle-dump`. The given implememtation will bring up an error, saying that the route names are not unique. This is because the way Pedestal works, it comes up for a name for each route. Since handle-dump was used as a handler for more than one route, the naming broke. This can be fixed by providing a vector, rather then just a handler. This will fix the proiblem but when handle-dump creates the pages, the data bein submitted does not appear. This is because the data is wrapped around the body part, which is provided by jetty. The data can be extracted from an interceptor `body-params`. To be able to use this interceptor, a new namespace `io.pedestal.http.body-params` has to be added. Is used as an interceptor generating function. After restarting the server, `:form-params` will include the title and the description.

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
                   :post [:todos#create handle-dump]}]]]])
    
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


> Exercise: Implement and wire up a create function in `todo.clj`. Hint: use a `let` a la hello world to  extract `[:form-params :title]` and `[:form-params :description]`. Once you've created a TODO, redirect to `(url-for :todos)`.

    (ns todoit.todo
      (:require [io.pedestal.interceptor :refer [defhandler]]
                [io.pedestal.http.route :refer [url-for]]
                [ring.util.response :refer [response redirect]]
                [datomic.api :as d]
                [todoit.todo.db :as db]))
    
    (defhandler create [req]
      (let [title (get-in req [:form-params "title"])
            description (get-in req [:form-params "description"])]
        (when title
          (print title)
          (db/create-todo title description))
        (redirect (url-for :todos))))
    
    (defhandler index [req]
      (let [todos (db/all-todos (d/db db/conn))]
        (response (str "<html>"
                         "<body>"
                           "<div>"
                           (if todos
                             (mapv :todo/title todos)
                             "<p>All done here!</p>")
                           "</div>"
                           "<form method=\"POST\" action=\"/todos\">"
                             "<input name=\"title\" placeholder=\"title\">"
                             "<input name=\"description\" placeholder=\"description\">"
                             "<input type=\"submit\">"
                           "</form>" 
                         "</body>"
                       "</html>"))))

## New paint job

Serving static files
Segregate views
Template pages via dsl.
How to link to routes using url-for

To give the app a better look, Twitter boostrap will be used. It has to be downloaded into `/resources/public`. Additional assets should be placed there as well. To actually serve the static resources, some ring middleware will be added to provide access to the static assets and file informations for the browser. The `io.pedestal.http.ring.middleware` will be added to the require declaration. Since ring is basically drop in compatible with pedestal, every ring middleware could be pulled apart and made interceptor compatible. To the list of interceptor two middleware will be added which are both interceptor generating functions. The middleware `middleware/resource` will be used to serve the resources pointing to the public directory containing the bootstrap library. The other middleware to be added is `middleware/file-info`. It will enrich header information with file sizes, content types etc, so that the browser is aware of the files being accessed.

    (ns todoit.core
      (:require [io.pedestal.http.route.definition :refer [defroutes]]
                [io.pedestal.http.route :as route :refer [router]]
                [io.pedestal.http :as http]
                [ns-tracker.core :refer [ns-tracker]]
                [ring.handler.dump :refer [handle-dump]]
                [io.pedestal.interceptor :refer [defon-request]]
                [todoit.todo :as todo]
                [io.pedestal.http.body-params :refer [body-params]]
                [io.pedestal.http.ring-middlewares :as middleware]))
    
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
                            (middleware/file-info)
                            (middleware/resource "public")
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

Restarting the browser and requesting [/bootstrap/css/bootstrap.min.css](http://localhost:8080/bootstrap/css/bootstrap.min.css) should display the contents of the file.

### Addig a template library

To provide a more convenient a flexible way for creating views, [hiccup](https://github.com/weavejester/hiccup) will be used. It provides an elegant way to create markup with Clojure syntax.

    (defproject todoit "0.1.0-SNAPSHOT"
      :description "FIXME: write description"
      :url "http://example.com/FIXME"
      :license {:name "Eclipse Public License"
                :url "http://www.eclipse.org/legal/epl-v10.html"}
      :dependencies [[org.clojure/clojure "1.6.0"]
                     [io.pedestal/pedestal.service "0.3.0-SNAPSHOT"]
                     [io.pedestal/pedestal.service-tools "0.3.0-SNAPSHOT"]
                     [io.pedestal/pedestal.jetty "0.3.0-SNAPSHOT"]
                     [ns-tracker "0.2.2"]
                     [ring/ring-devel "1.2.2"]
                     [com.datomic/datomic-free "0.9.4699"
                      :exclusions [org.slf4j/jul-to-slf4j
                                   org.slf4j/slf4j-nop]]
                     [hiccup "1.0.5"]]
      :main todoit.core)

Restarting the server will download the dependency. In `src/todoit` a new namepace will be added in file `view.clj`. The namespace will contain the function for rendering TODOs.

    (ns todoit.todo.view
      :require [io.pedestal.http.route :refer [url-for]]
               [hiccup.page :refer [html5]]
               [hiccup.core :refer [h]])

This will include `html5` for rendering the page according to the html 5 specification and `h` for a proper encoding of entities and so on.

~ 83:00