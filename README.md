# Football Store Microservices

A microservices-based e-commerce application for managing a football apparel store. This project demonstrates a complete microservices architecture using Spring Boot, with separate services for customers, products, inventory, and order management.

## What is it?

This is a distributed football store application that allows customers to browse and purchase football apparel (jerseys, shorts, shoes) from different warehouses. The system manages customer information, product catalog, warehouse inventory, and order processing through a set of independent microservices.

## Architecture

The application consists of 5 microservices:

- **API Gateway** (Port 8080) - Entry point that routes requests to appropriate services
- **Customers Service** - Manages customer profiles and registration (MySQL)
- **Apparels Service** - Handles football apparel catalog and pricing (PostgreSQL) 
- **Warehouses Service** - Manages warehouse locations and inventory (MySQL)
- **Orders Service** - Processes customer orders and tracks status (MongoDB)

## Technology Stack

- **Backend**: Spring Boot 3.4.4, Java 17
- **Build Tool**: Gradle
- **Databases**: MySQL, PostgreSQL, MongoDB
- **Containerization**: Docker & Docker Compose
- **API**: RESTful services with JSON

## Getting Started

### Prerequisites
- Docker and Docker Compose
- Java 17 (for local development)

### Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd football-microservice
   ```

2. **Start all services**
   ```bash
   docker-compose up -d
   ```

3. **Wait for services to be ready and run tests**
   ```bash
   ./test_all.bash start
   ```

4. **Access the application**
   - API Gateway: http://localhost:8080
   - API endpoints: http://localhost:8080/api/v1/{customers|apparels|warehouses|orders}

### Database Admin Tools

When running with Docker Compose, you can access:
- **phpMyAdmin** (Customers DB): http://localhost:5016
- **phpMyAdmin** (Warehouses DB): http://localhost:5015  
- **pgAdmin** (Apparels DB): http://localhost:9000
- **Mongo Express** (Orders DB): http://localhost:8085

### Sample API Usage

```bash
# Get all customers
curl http://localhost:8080/api/v1/customers

# Get all apparels
curl http://localhost:8080/api/v1/apparels

# Get all warehouses  
curl http://localhost:8080/api/v1/warehouses

# Get customer orders
curl http://localhost:8080/api/v1/customers/{customerId}/orders
```

## Domain Model

The application manages four main entities:
- **Customers**: Personal information, addresses, contact preferences
- **Apparels**: Football items (jerseys, shorts, shoes) with sizes and pricing
- **Warehouses**: Storage locations with capacity and address
- **Orders**: Customer purchases linking apparels from specific warehouses

## Development

Each microservice is a separate Spring Boot application that can be developed and deployed independently. The `create-projects.bash` script shows how the initial project structure was created using Spring Initializr.

## Testing

Run the comprehensive test suite:
```bash
./test_all.bash start stop
```

This will start all services, run API tests, and tear down the environment.