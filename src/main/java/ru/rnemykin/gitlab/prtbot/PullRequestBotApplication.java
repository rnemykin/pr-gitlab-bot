package ru.rnemykin.gitlab.prtbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan("ru.rnemykin.gitlab.prtbot.config.properties")
public class PullRequestBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(PullRequestBotApplication.class, args);
    }

}
