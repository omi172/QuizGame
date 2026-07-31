# Quiz App

A full-stack quiz application built with **Spring Boot**, **Thymeleaf**, and **MongoDB**. It supports two roles — **Admin** and **Participant** — with JWT-based authentication, admin-managed quizzes, timed quiz-taking, and automatic server-side scoring.

## Features

### User Authentication and Roles
- Registration and login via Spring Security with JWT.
- Users select their role at signup — **Admin** or **Participant**.
- JWT is issued on login and stored in an HttpOnly cookie, so it's carried automatically on normal page navigation (no manual header handling needed for a server-rendered app).
- Roles gate access: `/admin/**` requires `ROLE_ADMIN`, `/quiz/**` requires `ROLE_PARTICIPANT`.

### Question Management (Admin)
- Create, update, and delete quizzes — each with a title, description, and time limit.
- Add multiple-choice questions with any number of options, marking exactly one as correct.
- View all participant attempts and scores per quiz.

### Quiz Taking (Participant)
- Browse all available quizzes and select one to take.
- Questions render with multiple-choice options (all at once, in the current template).
- A live countdown timer enforces the quiz's time limit and auto-submits whatever is answered when it hits zero.
- The correct-answer flags are stripped out of the payload sent to the browser — a participant can never see the answer key via view-source or dev tools.

### Submission and Scoring
- On submit (manual or timer expiry), answers are graded server-side by comparing each selected option against the quiz's stored correct option.
- The resulting attempt — user ID, quiz ID, submitted answers, and score — is saved to MongoDB.
- Participants can view their full attempt history and individual results.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3 |
| Web / Views | Spring MVC + Thymeleaf |
| Database | MongoDB (Spring Data MongoDB) |
| Auth | Spring Security + JWT (jjwt 0.12) |
| Testing | JUnit 5, Mockito |
| Build | Maven |

## Project Structure

```
quiz-app/
├── src/main/java/com/quizapp/
│   ├── QuizApplication.java
│   ├── config/
│   │   └── SecurityConfig.java        # Stateless JWT security filter chain
│   ├── controller/
│   │   ├── AuthController.java        # Register / login / logout
│   │   ├── AdminQuizController.java   # Admin quiz & question CRUD
│   │   └── QuizController.java        # Participant browse / take / submit / history
│   ├── dto/
│   │   ├── RegisterRequest.java
│   │   ├── QuizDto.java / QuestionDto.java / OptionDto.java
│   │   ├── SubmitAttemptRequest.java / AnswerSubmission.java
│   ├── model/
│   │   ├── User.java / Role.java
│   │   ├── Quiz.java / Question.java / Option.java
│   │   └── QuizAttempt.java / Answer.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── QuizRepository.java
│   │   └── QuizAttemptRepository.java
│   ├── security/
│   │   ├── JwtUtil.java               # Token generation / validation
│   │   └── JwtAuthFilter.java         # Reads JWT cookie, populates SecurityContext
│   └── service/
│       ├── UserService.java           # Registration + UserDetailsService
│       ├── QuizService.java           # Quiz/question CRUD, answer-key sanitization
│       └── QuizAttemptService.java    # Grading + persistence
├── src/main/resources/
│   ├── application.properties
│   ├── static/css/style.css
│   └── templates/
│       ├── fragments/header.html
│       ├── login.html / register.html
│       ├── admin/quiz-list.html, quiz-form.html, quiz-results.html
│       └── quiz/list.html, take.html, result.html, history.html
├── src/test/java/com/quizapp/service/
│   ├── UserServiceTest.java
│   ├── QuizServiceTest.java
│   └── QuizAttemptServiceTest.java
└── pom.xml
```

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- MongoDB running locally on `27017` (or update the URI below)

### Configuration
Edit `src/main/resources/application.properties` if needed:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/quizdb
jwt.secret=ChangeThisSecretKeyToSomethingLongAndRandomInProduction123456
jwt.expiration-ms=86400000
server.port=8080
```

**Change `jwt.secret` before any real deployment** — the committed value is a placeholder.

### Run

```bash
mvn spring-boot:run
```

The app will be available at `http://localhost:8080`.

### Run Tests

```bash
mvn test
```

Covers `UserService` (registration, role assignment, role→authority mapping), `QuizService` (quiz creation, ID generation, answer-key sanitization for participants), and `QuizAttemptService` (full-credit, partial-credit, and unanswered-question scoring).

## Usage Flow

1. **Register** at `/register`, choosing Admin or Participant.
2. **Admin**: log in → `/admin/quizzes/new` → add a title, description, time limit, and questions (mark one correct option per question) → save.
3. **Participant**: log in → `/quiz/list` → pick a quiz → answer within the time limit → submit (or let the timer auto-submit) → view score at `/quiz/result/{attemptId}` → review past attempts at `/quiz/history`.

## Known Trade-off

Registration lets a user select **Admin** for themselves, per the original spec ("When users sign up, assign them roles: Admin or Participant"). This is fine for a learning/portfolio project, but in a real deployment self-service admin selection is a privilege-escalation risk — admin accounts should instead be created by an existing admin or seeded at startup, not chosen freely at signup.
