<div align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-3.4.3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React" />
  <img src="https://img.shields.io/badge/Apache_Kafka-7.3.2-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white" alt="Kafka" />
  <img src="https://img.shields.io/badge/Redis-Alpine-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker" />
</div>

<br />
<div align="center">
  <h1 align="center">🎓 EduTrack: Advanced School Management & Live Transit System</h1>
  <p align="center">
    A production-ready, full-stack enterprise web application built to modernize school administration. Featuring Role-Based Access Control, automated workflows, and a <strong>Real-Time Live Bus Tracking pipeline</strong> powered by Kafka and WebSockets.
  </p>
</div>

<hr />

## 🌟 Project Overview

This project is a comprehensive **School Management System (SMS)** designed to handle the daily operations of an educational institution. It moves beyond standard CRUD applications by integrating a **High-Performance Real-Time Data Pipeline** to solve a real-world problem: Live School Bus Tracking for parents and administrators.

It is designed with scalability, robust security, and an exceptional user experience in mind, serving four distinct roles: `ADMIN`, `TEACHER`, `STUDENT`, and `STAFF`.

---

## 🚀 Core Features

### 🏢 1. Core Administration
* **Role-Based Access Control (RBAC):** Secure authentication utilizing JWT (JSON Web Tokens) with strict endpoint protection.
* **Admissions & Academics:** End-to-end management of class creation, subject mapping, and student enrollment.
* **Attendance & Leave:** Automated attendance registers and a multi-level leave approval workflow.

### 🚌 2. Real-Time Bus Tracking Pipeline (Highlight Feature)
* **Live GPS Ingestion:** A high-throughput pipeline utilizing **Apache Kafka** to ingest real-time GPS coordinates from moving school buses.
* **State Management:** **Redis** is used as a fast, in-memory cache to store the absolute latest coordinates of every active bus, ensuring zero database bottleneck.
* **Live Broadcasting:** **WebSockets (STOMP)** stream the cached coordinates directly to connected clients without long-polling.
* **Interactive UI:** The frontend uses **Leaflet.js** to render moving bus icons smoothly on an interactive map.
* **Simulation Engine:** Includes a custom-built "Bus Simulator" that mocks driver movement across the city for dynamic project demonstrations.

---

## 🛠️ Technology Stack

**Backend System**
* **Java 21 & Spring Boot 3.4.x**: Core application framework.
* **Spring Security & JWT**: Stateless authentication and authorization.
* **Spring Data JPA & Hibernate**: ORM and database interactions.
* **Apache Kafka & Zookeeper**: Distributed event streaming for high-frequency GPS data.
* **Redis**: Fast, in-memory caching for live vehicle coordinates.
* **PostgreSQL 16**: Primary relational database.

**Frontend Interface**
* **React 18 & Vite**: Blazing fast frontend SPA.
* **React Router Dom**: Client-side routing with protected route logic.
* **Lucide React**: Modern, consistent iconography.
* **Leaflet.js & React-Leaflet**: Interactive map rendering for the transit module.

**DevOps & Deployment**
* **Docker & Docker Compose**: Full containerization of the database, message brokers, caching layer, and application servers.
* **Nginx**: Reverse proxying API requests and serving static frontend files.
* **AWS EC2**: Cloud hosting and deployment environment.

---

## 🧠 System Architecture & Data Flow

### The Real-Time Transit Architecture
1. **Producer (Driver App/Simulator):** Sends HTTP POST requests containing Lat/Lng to the `/api/transport/location` endpoint.
2. **Event Stream (Kafka):** The Spring Boot backend instantly publishes this data as an event to the `bus-location-events` Kafka Topic.
3. **Consumer & Cache:** A Spring Kafka Consumer listens to the topic, processes the event, and updates the latest location in **Redis** (`bus:location:{id}`).
4. **Broadcaster:** Simultaneously, the backend pushes the new coordinates to the `/topic/bus/{id}` WebSocket channel.
5. **Client (Parent UI):** React clients subscribed to the WebSocket instantly receive the payload and animate the bus marker on the Leaflet map.

---

## 👨‍💻 Skills Demonstrated

This project showcases my ability to architect and deliver complex, modern software solutions:

* **Enterprise Backend Architecture:** Designing robust Spring Boot APIs handling complex relational entities (JPA/Hibernate).
* **Event-Driven Microservices:** Utilizing Kafka to decouple high-throughput data ingestion from core monolithic operations.
* **Real-Time Web Technologies:** Implementing bidirectional communication via WebSockets and STOMP.
* **Modern Frontend Engineering:** Building clean, responsive, and dynamic React user interfaces without relying on heavy CSS frameworks.
* **DevOps & Cloud Deployment:** Containerizing multi-tier applications and orchestrating them via Docker Compose on AWS infrastructure.

---

## ⚙️ Local Setup & Installation

### Prerequisites
* Docker and Docker Compose installed.
* Git installed.

### 1. Clone the Repository
```bash
git clone https://github.com/Ramcharan2012/School_Management.git
cd School_Management
```

### 2. Bootstrapping with Docker Compose
This project is fully containerized. A single command will spin up PostgreSQL, Redis, Kafka, Zookeeper, the Spring Boot Backend, and the React Frontend.

```bash
docker-compose up -d --build
```

### 3. Accessing the Application
* **Frontend UI:** `http://localhost:80`
* **Swagger API Docs:** `http://localhost:8080/api/swagger-ui.html`

### 4. Demo Login Credentials
Upon the first startup, the database automatically seeds a secure Admin account:
* **Email:** `admin@school.com`
* **Password:** `Admin@123`

*(Note: Use the "Transport Setup" and "Bus Simulator" pages inside the Admin Dashboard to witness the real-time tracking in action!)*

---

<div align="center">
  <i>Developed with ❤️ for modern educational infrastructure.</i>
</div>
