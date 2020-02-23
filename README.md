# IBCService
Investment Bank Console

Example project of a µIBCService with the following technologies;
* Java 8
* Spring Boot 2.2.4.RELEASE
* Spring Framework 5.2.3.RELEASE
* Hibernate 5.4.4.Final - Not used yet
* MySql 8.0.17 (Connector) - Not used yet
* Swagger2 2.9.2
* Lombok 1.18.12
* JUnit 5.5.1
* Mockito 3.0.0

Service will serve the following petitions;
* Process Initial Balance
* Process Order
* Get cached Map (Test purpose)

# Things to consider
1. Modify the path for the logger in the src/main/resources/logback.xml
2. Modify application-default.yml when connecting to DB also if want to change the port for the service.
