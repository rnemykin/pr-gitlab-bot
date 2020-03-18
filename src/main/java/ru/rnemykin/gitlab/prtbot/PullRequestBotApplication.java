package ru.rnemykin.gitlab.prtbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableSpringDataWebSupport
@ConfigurationPropertiesScan("ru.rnemykin.gitlab.prtbot.config.properties")
public class PullRequestBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(PullRequestBotApplication.class, args);
    }

}
