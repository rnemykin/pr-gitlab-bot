package ru.rnemykin.gitlab.prtbot.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Data
@ConfigurationProperties("app.gitlab.pr")
public class CheckPullRequestProperties {
    private List<String> userNames;
    private List<Integer> userIds;

    @DurationUnit(ChronoUnit.DAYS)
    private Duration skippPrDaysPassCount;
    private Duration regularMessagesTtl;
}
