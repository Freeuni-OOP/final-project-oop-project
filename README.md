# Quiz Website (Spring Boot)

CS108 Quiz Website migrated from the legacy JSP/Servlet/DAO application to a
Spring Boot application using Spring MVC, Spring Data JPA/Hibernate, Thymeleaf,
and Lombok.

## Requirements

- JDK 21 or newer
- Maven 3.9 or newer
- IntelliJ IDEA is recommended for local development

MySQL/MariaDB is optional. The default configuration uses an in-memory H2
database so the project can run immediately after opening it.

## Default Database

The app runs with H2 by default:

```properties
spring.datasource.url=jdbc:h2:mem:quiz_website;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=
```

Hibernate creates the tables automatically with:

```properties
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=never
```

`src/main/resources/schema.sql` is kept as a reference for the table layout. It
does not need to be run manually for the default H2 setup.

## Demo Data

Demo seed data is enabled by default:

```properties
quizwebsite.seed.enabled=true
```

The app creates these users on first startup when the database is empty:

```text
admin / admin123
alex  / password
sam   / password
```

The seed also creates sample quizzes, questions, attempts, friendships,
favorites, ratings, achievements, and an admin announcement.

To disable demo data:

```bash
SEED_DATA=false mvn spring-boot:run
```

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

## Optional MySQL/MariaDB Setup

For persistent local data, override the datasource with environment variables:

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
quizwebsite.seed.enabled=true
```

Hibernate still creates/updates the schema automatically. `schema.sql` is only a
reference unless you choose to manage the database manually.

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

The test profile uses H2 and disables demo seed data.

## Architecture

```text
com.quizwebsite
|-- QuizWebsiteApplication
|-- config
|-- model
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
