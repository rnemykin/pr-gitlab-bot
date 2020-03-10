package ru.rnemykin.gitlab.prtbot.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties("app.gitlab.pr")
public class CheckPullRequestProperties {
    private List<String> userNames;
    private List<Integer> userIds;
}
