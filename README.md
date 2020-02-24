# IBCService
Investment Bank Console

Example project of a µIBCService with the following technologies;
* Java 8
* Spring Boot 2.2.4.RELEASE
* Spring Framework 5.2.3.RELEASE (Core, Boot, MVC, Data)
* Hibernate 5.4.4.Final
* Swagger2 2.9.2
* Lombok 1.18.12
* JUnit 5.5.1
* Mockito 3.0.0
* ModelMapper 2.3.6

Service will serve the following petitions;
* Process Initial Balance
* Process Order
* Get cached Map (Test purpose)

# Things to consider
1. Modify the path for the logger in the src/main/resources/logback.xml
2. Modify application-default.yml when connecting to DB also if want to change the port for the service.
3. To run this application you need to have git and maven installed in the desired machine to work.
4. Once the project is cloned use mvn clean install spring-boot:run (this will clean the project, install all the libraries needed, run tests and finally run the application).
5. You can test the endpoints by curl, postman/soapui or with swagger (http://localhost:63000/swagger-ui.html).
6. It's possible to see the DB at http://localhost:63000/h2-console (user:sa | no password set)
