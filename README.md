# Network Watcher

A comprehensive network monitoring and security scanning application built with Spring Boot (Backend) and React (Frontend).

## Features

- **Network Device Discovery** - Automatically discover devices on your network
- **SNMP Integration** - Retrieve device information using SNMP protocol
- **Vulnerability Scanning** - Scan devices for security vulnerabilities using Nmap
- **Real-time Monitoring** - WebSocket-based real-time notifications
- **Bandwidth Tracking** - Monitor network bandwidth usage
- **Alert System** - Email and real-time alerts for security issues
- **User Authentication** - JWT-based secure authentication
- **Audit Logging** - Track all system activities

## Project Structure

```
Network_Watcher/
├── backend/          # Spring Boot backend application
└── frontend/         # React frontend application
```

## Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL (for production) or H2 (for development)
- Nmap (for vulnerability scanning)
- Maven

## Getting Started

### Backend Setup

```bash
cd backend
./mvnw spring-boot:run
```

Backend runs on `http://localhost:8080`

### Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`

## Default Credentials

- Username: `admin`
- Password: `admin123`

## Technologies Used

### Backend
- Spring Boot 4.0.2
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL/H2 Database
- SNMP4J for SNMP operations
- WebSocket for real-time updates

### Frontend
- React 19
- Material-UI (MUI)
- Axios for API calls
- React Router for navigation
- Recharts for data visualization
- STOMP/SockJS for WebSocket

## Deployment

See deployment documentation for Docker and cloud deployment options.

## License

This project is for educational purposes.
