# CodeForge AI

CodeForge AI is a full-stack coding interview preparation and competitive programming platform inspired by modern online judges such as LeetCode.

The platform allows users to solve coding problems, execute and submit Java solutions, receive AI-powered assistance, participate in contests, and track their coding progress through a personalized dashboard.

---

## Features

### Authentication

* JWT-based Authentication
* User Registration
* User Login
* Secure Route Protection

### Coding Workspace

* Monaco Code Editor
* Java Code Execution
* Code Submission System
* Execution Results
* Submission Tracking

### AI Features

* AI Hints
* AI Assistance for Problem Solving
* Context-Aware Guidance

### Problem Solving

* Multiple Coding Problems
* Difficulty Categorization
* Problem Descriptions
* Editorial Section
* Solution Section
* Submission History

### Contest Module

* Contest Listing
* Contest Participation
* Contest Problem Sets
* Basic Contest Management

### Dashboard

* User Statistics
* Problems Solved
* Submission Metrics
* Contest Participation Overview

---

## Tech Stack

### Frontend

* React
* TypeScript
* Tailwind CSS
* Monaco Editor
* Axios

### Backend

* Spring Boot
* Java
* Spring Security
* JWT Authentication
* Maven

### Database

* MySQL

### AI Integration

* Google Gemini API

---

## Project Structure

```text
CodeForge_AI/
│
├── frontend/
│   ├── src/
│   └── public/
│
├── backend/
│   ├── src/
│   └── resources/
│
└── README.md
```

---

## Getting Started

### Clone Repository

```bash
git clone https://github.com/rohitchell87/CODEFORGE_AI.git
cd CODEFORGE_AI
```

---

## Backend Setup

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

---

## Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on:

```text
http://localhost:5173
```

---

## Environment Configuration

Update:

```text
backend/src/main/resources/application.properties
```

with your local configuration values.

Example:

```properties
spring.datasource.url=YOUR_DATABASE_URL
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

ai.api-key=YOUR_API_KEY
```

---

## Screenshots

Add screenshots of:

<img width="1890" height="904" alt="image" src="https://github.com/user-attachments/assets/3a9cee94-7ff5-4d1b-b2ca-cca5b875a60b" />
<img width="1890" height="904" alt="image" src="https://github.com/user-attachments/assets/d9678474-de4b-46f1-8371-9bc1a2b62a15" />
<img width="1890" height="904" alt="image" src="https://github.com/user-attachments/assets/531b77f9-02c9-45ab-b04a-1fba8037ef09" />
<img width="1890" height="904" alt="image" src="https://github.com/user-attachments/assets/d3b8764e-1b7c-4978-97fb-f566cd53aac1" />
<img width="1890" height="904" alt="image" src="https://github.com/user-attachments/assets/4effa639-c0b6-4ac6-906c-62b33ae974b7" />

---

## Future Improvements

* Advanced contest rankings
* AI code review
* Company-specific problem sets
* Discussion forums
* Performance analytics

---

## Author

Rohit Sharma
B.Tech CSE, IIIT Delhi
