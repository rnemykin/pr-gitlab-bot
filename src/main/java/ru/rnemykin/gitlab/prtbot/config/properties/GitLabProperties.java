package ru.rnemykin.gitlab.prtbot.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("app.gitlab")
public class GitLabProperties {
    private String url;
    private String token;
}
