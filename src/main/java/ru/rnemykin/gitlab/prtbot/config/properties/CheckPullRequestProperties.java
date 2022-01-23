package ru.rnemykin.gitlab.prtbot.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Data
@ConfigurationProperties("app.gitlab.pr")
public class CheckPullRequestProperties {
    private List<String> userNames;
    private List<PrAuthor> authors;

    @DurationUnit(ChronoUnit.DAYS)
    private Duration skippPrDaysPassCount;
    private Duration regularMessagesTtl;


    public boolean isJuniorDeveloper(int id) {
        return authors.stream().anyMatch(author -> id == author.id && Boolean.TRUE.equals(author.junior));
    }

    public boolean isFreshMeat(int id) {
        return authors.stream().anyMatch(author -> id == author.id && Boolean.TRUE.equals(author.freshMeat));
    }

    public List<Integer> getAuthorIds() {
        return authors.stream().map(PrAuthor::getId).collect(toList());
    }

    @Data
    public static class PrAuthor {
        private Integer id;
        private Boolean junior = false;
        private Boolean freshMeat = false;
    }
}
