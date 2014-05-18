# todoit

A CRUD application based on Ryan Neufeld's Pedastal workshop.

# Project setup

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

To have a more continuous workflow, a library called ```ns-tracker``` is added. It provides an  observation mechanism for files and when these change. This is added as a dependency into
```project.clj```.

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










