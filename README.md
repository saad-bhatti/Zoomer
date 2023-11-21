# JavaMicroservices

## Team

This project was developed was completed in partnership with Awaiz Aziz (https://www.linkedin.com/in/awais-aziz-4bb7a318a/)

## Project Description

This project involves the development of a microservices architecture for a web application. The application consists of three microservices, each utilizing a different database (PostgreSQL, MongoDB, and Neo4J). The development is carried out using the Java Maven framework, and Docker will is employed to containerize the services. Additionally, an API Gateway is implemented to interface with users and coordinate communication between the microservices.

## Objectives

- Understand the design and purpose of microservices architecture.
- Implement an API Gateway to manage user requests and facilitate communication with microservices.
- Create REST API endpoints supported by Neo4j graph, MongoDB, and PostgreSQL databases.
- Apply git, git flow, and adhere to a consistent code style.
- Gain proficiency in Docker and containerization principles.
- Development Technologies
- Java Maven framework
- PostgreSQL, MongoDB, Neo4J databases
- Docker for containerization

## Contributions

- Microservices Implementation: Development of three backend Java APIs as microservices, interacting with the MongoDB, PostgreSQL, and Neo4J databases.
- API Gateway Implementation: Development of the API Gateway to act as a proxy server between users and the microservices, ensuring proper routing of requests and responses.
- REST API Endpoints: Creation of the REST API endpoints for the microservices, adhering to the specifications provided.
- Error Handling: APIs return appropriate status codes (200-OK, 400-BAD REQUEST, 403-FORBIDDEN, 404-NOT FOUND, 500-INTERNAL SERVER ERROR) based on the success or failure of operations.
- Testing: Development of two tests for each implemented endpoint, covering both successful and failed responses. All tests are directed to the API Gateway.
- CI/CD: Implementation of testing procedures for each endpoint, focusing on successful and failed responses.
