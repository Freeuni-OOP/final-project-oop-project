# Quiz Website (Spring Boot)

CS108 Quiz Website built with Spring MVC, Spring Data JPA/Hibernate, Thymeleaf,
and Lombok.

## Requirements

- JDK 21 or newer
- Maven 3.9 or newer
- IntelliJ IDEA is recommended for local development
- MySQL or MariaDB running locally

## Running From IntelliJ

1. Open the project folder in IntelliJ.
2. Let IntelliJ import/reload the Maven project from `pom.xml`.
3. Set the Project SDK to JDK 21 or newer.
4. Open `src/main/java/com/quizwebsite/QuizWebsiteApplication.java`.
5. Run `QuizWebsiteApplication`.

Open:

```text
http://localhost:8080/login
```

If port `8080` is already in use, add this to the run configuration's program
arguments:

```text
--server.port=8081
```

Then open:

```text
http://localhost:8081/login
```

## Running From Terminal

```bash
mvn test
mvn spring-boot:run
```

Build and run the executable jar:

```bash
mvn clean package
java -jar target/quiz_website.jar
```

If `mvn` is not available in your shell, use IntelliJ's Maven panel or install
Maven locally.

## Database Setup

The app connects to MySQL/MariaDB. By default it uses `root` with no password
on `localhost:3306` and creates the `quiz_website` database itself
(`createDatabaseIfNotExist=true`). Hibernate creates and updates the tables
automatically from the `@Entity` classes (`ddl-auto=update`) — there's no
`schema.sql` file, so nothing needs to be run manually.

A small set of demo data is created automatically on first startup, if the
database is empty:

```text
admin / admin123
alex  / password
sam   / password
```

`admin` is already promoted to an admin account. The seed also creates a
welcome announcement, two sample quizzes with questions, a friendship between
`alex` and `sam`, some quiz history, a couple of favorites/ratings, and a
few achievements — enough that the app isn't empty on first login.

To disable the seed (e.g. once you have real data you don't want touched), set:

```properties
quizwebsite.seed.enabled=false
```

To use different credentials, override the datasource with environment
variables:

```bash
DB_URL="jdbc:mysql://localhost:3306/quiz_website?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true" \
DB_USERNAME=root \
DB_PASSWORD=your_password \
mvn spring-boot:run
```

You can also create a local, git-ignored file:

```text
src/main/resources/application-local.properties
```

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/quiz_website?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
```

## Features

- User registration and login with BCrypt password hashing
- Admin dashboard, announcements, user management, quiz removal, and report resolution
- Quiz creation and editing
- Question types:
  - question response
  - fill in the blank
  - multiple choice
  - picture response
  - multi-answer
  - multi-select
- Random question order, multi-page quizzes, immediate correction, and practice mode
- Quiz history, scoring, leaderboards, and recent activity
- Friend requests, friend activity, messages, and challenges
- Achievements
- Saved/favorite quizzes
- Ratings and reviews
- Quiz reports
- Tags and categories
- XML quiz import
- Profile fields and privacy settings

## Tests

Run:

```bash
mvn test
```

The test profile uses an in-memory H2 database, so tests don't need a real
MySQL/MariaDB instance running.

## Architecture

```text
com.quizwebsite
|-- QuizWebsiteApplication
|-- config
|-- model
|   |-- question
|   |-- social
|   |-- achievement
|   `-- activity
|-- repository
|-- service
|-- web
`-- util

resources
|-- static/css
|-- templates
`-- application.properties
```

The question hierarchy uses JPA inheritance. Each question type implements its
own grading behavior, while Hibernate loads the correct subclass from the
database.
