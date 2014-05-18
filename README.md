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
