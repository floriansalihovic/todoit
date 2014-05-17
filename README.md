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
Next, a ```:main``` is added, pointing to the default namespace provided by the standard lein template.


    :main todoit.core



