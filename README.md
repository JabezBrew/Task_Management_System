# Task Management System 🗃️

A robust Spring Boot application for managing tasks and user collaborations on these tasks.

## Features 🌟

- User Registration and Authentication 🔐
  - JWT-based authentication
  - Secure password storage
  - Email validation

- Task Management 📋
  - Create new tasks
  - Assign tasks to users
  - Track task status (CREATED, IN_PROGRESS, COMPLETED)
  - Filter tasks by author and assignee

- Comments System 💬
  - Add comments to tasks
  - View comments in chronological order
  - Track comment count per task

## Tech Stack 💻

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL Database
- Gradle
- Lombok
- JWT Authentication

## API Endpoints 🛣️

### Authentication
- `POST /api/accounts` - Register new user
- `POST /api/auth/token` - Get JWT token

### Tasks
- `POST /api/tasks` - Create new task
- `GET /api/tasks` - Get all tasks (with optional author/assignee filters)
- `PUT /api/tasks/{taskId}/assign` - Assign task to user
- `PUT /api/tasks/{taskId}/status` - Update task status

### Comments
- `POST /api/tasks/{taskId}/comments` - Add comment to task
- `GET /api/tasks/{taskId}/comments` - Get task comments

## Running the Application 🚀

1. Clone the repository
2. Configure MySQL database in `application.properties`
3. Run using Gradle:
```bash
./gradlew bootRun
```

## Security 🔒

- Basic HTTP Authentication + JWT tokens
- Password encryption
- Role-based access control
- API endpoint protection

## Data Models 📊

### User
- Email (unique)
- Password (encrypted)
- Tasks (one-to-many)

### Task
- Title
- Description
- Status
- Author
- Assignee
- Comments

### Comment
- Text
- Author
- Task reference
- Timestamp

## Contributing 🤝

1. Fork the project
2. Create feature branch
3. Commit changes
4. Push to branch
5. Open pull request

## Acknowledgments 🙏

- Project created as part of Hyperskill's Java Backend Developer track
- Uses Spring Boot's comprehensive framework
- Implements modern security practices
