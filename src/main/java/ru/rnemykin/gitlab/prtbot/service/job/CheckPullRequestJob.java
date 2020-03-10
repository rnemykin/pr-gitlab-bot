package ru.rnemykin.gitlab.prtbot.service.job;

import lombok.RequiredArgsConstructor;
import org.gitlab4j.api.models.User;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.rnemykin.gitlab.prtbot.config.properties.CheckPullRequestProperties;
import ru.rnemykin.gitlab.prtbot.service.client.gitlab.GitLabServiceClient;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CheckPullRequestJob {
    private static List<Integer> userIds;

    private final GitLabServiceClient client;
    private final CheckPullRequestProperties checkPullRequestProperties;


    @EventListener(ApplicationStartedEvent.class)
    public void afterStart() {
        userIds = !CollectionUtils.isEmpty(checkPullRequestProperties.getUserIds())
                ? checkPullRequestProperties.getUserIds()
                : checkPullRequestProperties.getUserNames()
                        .stream()
                        .map(client::findByName)
                        .filter(Objects::nonNull)
                        .map(User::getId)
                        .collect(Collectors.toList());
    }

    @Scheduled(fixedDelay = 60 * 1000)
    public void notifyAboutOpenedPr() {

    }


}
