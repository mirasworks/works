# works 
# Java http server based on jboss netty.

The main goal is to build a simple http server really easy to use for fast application writting. We don't want all the maven pom things and all the boilerplate configuration files written in xml, nor the deploiement process. 

Works is inspired by concepts and architecture I liked in different http servers written in different languages.
* python django
* php symphony
* java jetty
* java playframework

The project is in an early experimental phase and the API will probably changes a lot until the version 1
There is a lot to do.

For the moment it can serve a request down to a controller and a response from it and could be used backed from nginx for static serving. But there is no ajax json for the moment.



Milestone 1 let's get started
* Ok static serving 
* random file up/down from the controller
* dynamic reload of configuration
* a lot of security improvement
* session and cookies management
* client project generation from command
* errors in html templates while dev mode enabled
* stop and restart by command line
* more commands line

Milestone 2 do it better
* load tests and benchmarks
* instrumentation
* caching and optimizations
* RFC2616 full implementation (a lot of work)
* documentation serving module
* split static and controller mechanics in modules
* make it more hackable to allow everyone to replace and modify components


 

