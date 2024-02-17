# Zoomer

## Team

This project was developed was completed in partnership with [Awaiz Aziz](https://www.linkedin.com/in/awais-aziz-4bb7a318a/).

## About Zoomer

This project involves the development of a microservices architecture for a web application. The application consists of three microservices, each utilizing a different database (PostgreSQL, MongoDB, and Neo4j). The development is carried out using the Java Maven framework, and Docker will is employed to containerize the services. Additionally, an API Gateway is implemented to interface with users and coordinate communication between the microservices.

## Usage

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)
- [Python](https://www.python.org/downloads/)

### Running the microservices

1. Clone the repository
2. Navigate to the root directory of the repository
3. Run `docker-compose up -d` to start the application

_The api gateway will be accessible on `localhost:8004`_

### Stopping the microservices

Given the application is running:

1. Navigate to the root directory of the repository
2. Run `docker-compose down` to stop the application

### Initializing data in the databases

1. Run the microservices (as described above)
2. Run `python setupData.py` to initialize the databases with data

### Testing the microservices

1. Run the microservices (as described above)
2. Enter a microservice container by running `docker exec -it <container_name>  bash` where `<container_name>` is the name of the container you want to test.
3. Run `mvn test` to run all tests specific to the microservice you are in.

## Objectives

- Understand the design and purpose of microservices architecture.
- Implement an API Gateway to manage user requests and facilitate communication with microservices.
- Create REST API endpoints supported by Neo4j graph, MongoDB, and PostgreSQL databases.
- Apply git, git flow, and adhere to a consistent code style.
- Gain proficiency in Docker and containerization principles.
- Develop within the Java Maven framework
- Interact with PostgreSQL, MongoDB, Neo4J databases, helping to understand the differences between SQL and NoSQL databases.

## Tasks

- **Microservices Implementation**: Development of three backend Java APIs as microservices, interacting with the MongoDB, PostgreSQL, and Neo4J databases.
- **API Gateway Implementation**: Development of the API Gateway to act as a proxy server between users and the microservices, ensuring proper routing of requests and responses.
- **REST API Endpoints**: Creation of the REST API endpoints for the microservices, adhering to the specifications provided.
- **Error Handling**: APIs return appropriate status codes based on the success or failure of operations.
- **Testing**: Development of JUnit tests for each implemented endpoint, covering both successful and failed responses.

## File Structure

- **`ApiGateway/`** - Contains the code for the API Gateway.
- **`LocationMicroservice/`** - Contains the code for the location microservice, which interacts with the Neo4j database.
- **`TripInfoMicroservice/`** - Contains the code for the trip info microservice, which interacts with the MongoDB database.
- **`UserMicroservice/`** - Contains the code for the user microservice, which interacts with the PostgreSQL database.
- **`docker-compose.yml`** - Defines configurations for the docker containers.
- **`README.md`** - This file!
- **`setupData.py`** - Contains the code to initialize the databases with data.
