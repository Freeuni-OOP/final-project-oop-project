package com.quizwebsite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Quiz Website.
 *
 * Packaged as an executable jar with an embedded Tomcat — run with
 * {@code mvn spring-boot:run} or {@code java -jar target/quiz_website.jar}.
 */
@SpringBootApplication
public class QuizWebsiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizWebsiteApplication.class, args);
    }
}
