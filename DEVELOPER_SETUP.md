# 🏫 School Management System — Developer Setup Guide

This guide explains exactly how to start the entire project from scratch on a new machine. Follow these steps **in order** every time you want to run the project.

---

## ✅ One-time Prerequisites (Install these once)

| Tool | Version | Download |
|---|---|---|
| **Java (JDK)** | 21 | https://adoptium.net |
| **Maven** | 3.9+ | https://maven.apache.org |
| **Node.js** | 18+ LTS | https://nodejs.org |
| **Docker Desktop** | Latest | https://www.docker.com/products/docker-desktop |
| **IntelliJ IDEA** | Community/Ultimate | https://www.jetbrains.com/idea |
| **Git** | Latest | https://git-scm.com |

> **To verify:** Open a terminal and run `java -version`, `mvn -version`, `node -version`, `docker -version`. All should print a version number.

---

## 📂 Monorepo Folder Structure

```text
School_Project/                    ← Open THIS folder in IntelliJ
├── School_Management/             ← Spring Boot Backend
│   ├── src/main/java/...
│   ├── docker-compose.yml         ← Start infrastructure from here
│   └── pom.xml
└── frontend/                      ← React UI (Phase 5)
    ├── src/
    └── package.json
```

---

## 🐳 Step 1: Start Docker Infrastructure (EVERY TIME)

Docker must be running before you start the Spring Boot app.

```bash
# Open a terminal in the School_Management folder
cd D:\INTELLJ_PROJECTS\School_project\School_Management

# Start PostgreSQL + Redis + Zookeeper + Kafka in the background
docker-compose up -d
```

> ✅ **Verify it's working:**
> ```bash
> docker ps
> ```
> You should see 4 containers: `school-postgres`, `school-redis`, `school-zookeeper`, `school-kafka`.

To stop all containers when done:
```bash
docker-compose down
```

---

## ☕ Step 2: Start the Spring Boot Backend

Open a **new terminal tab** in IntelliJ (bottom of the screen):

```bash
cd D:\INTELLJ_PROJECTS\School_project\School_Management
mvn spring-boot:run
```

> ✅ **The backend is ready when you see:**
> ```
> Started SchoolManagementApplication in XX seconds
> Tomcat started on port 8080
> ```

| Service | URL |
|---|---|
| API Base URL | `http://localhost:8080/api` |
| Swagger UI | `http://localhost:8080/api/swagger-ui/index.html` |

---

## 🌐 Step 3: Start the React Frontend UI

Open a **second terminal tab** in IntelliJ:

```bash
cd D:\INTELLJ_PROJECTS\School_project\frontend
npm install     # Only needed first time or after pulling new code
npm run dev
```

> ✅ **The frontend is ready when you see:**
> ```
> VITE v5.x.x  ready in XXX ms
> ➜  Local:   http://localhost:5173/
> ```

Open your browser and go to `http://localhost:5173` to see the UI.

---

## 📋 Startup Checklist (Every session)

```text
[ ] 1. Open Docker Desktop app (must be running in system tray)
[ ] 2. cd School_Management → docker-compose up -d
[ ] 3. Run Spring Boot (mvn spring-boot:run) in Terminal 1
[ ] 4. Run React frontend (npm run dev) in Terminal 2
[ ] 5. Open http://localhost:5173 in browser
```

---

## 🗄️ Useful Docker Commands

```bash
docker ps                  # See running containers
docker logs school-kafka   # See Kafka logs
docker-compose down        # Stop everything
docker-compose down -v     # Stop AND wipe all data (fresh start)
```

---

## 🚨 Common Problems & Fixes

| Problem | Fix |
|---|---|
| `not found: docker-compose` | Run from `School_Management/` folder, not `School_Project/` |
| `Port 8080 already in use` | Kill the other app using that port |
| `Connection refused: localhost:9092` | Kafka not started. Run `docker-compose up -d` first |
| `npm: not recognized` | Node.js not installed. Download from nodejs.org |
| `LEADER_NOT_AVAILABLE` in logs | Normal. Kafka warming up. Waits 10 seconds, auto-resolves |
