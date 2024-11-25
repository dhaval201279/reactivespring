# Reference Implementation of Josh Long's Reactive Spring book along with additional information

## Chapter 5
### AsyncApiIntegrationTest
**FluxSink** - Wrapper API around a downstream Subscriber for emitting any number of next signals followed by zero or one 
onError/onComplete. It also has *FluxSink.OverflowStrategy* with enumerated values for back pressure handling



=============================================================
# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.5.4/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.5.4/maven-plugin/reference/html/#build-image)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/2.5.4/reference/htmlsingle/#using-boot-devtools)
* [Spring Data R2DBC](https://docs.spring.io/spring-boot/docs/2.5.4/reference/html/spring-boot-features.html#boot-features-r2dbc)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/2.5.4/reference/htmlsingle/#production-ready)

### Guides
The following guides illustrate how to use some features concretely:

* [Acessing data with R2DBC](https://spring.io/guides/gs/accessing-data-r2dbc/)
* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)

### Additional Links
These additional references should also help you:

* [R2DBC Homepage](https://r2dbc.io)

## Missing R2DBC Driver

Make sure to include a [R2DBC Driver](https://r2dbc.io/drivers/) to connect to your database.

# Command to run all tests including Gatling tests
````
mvn clean test -Pgatling-tests
