package ru.rnemykin.gitlab.prtbot.service;

import lombok.RequiredArgsConstructor;
import org.gitlab4j.api.models.User;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.rnemykin.gitlab.prtbot.config.properties.CheckPullRequestProperties;
import ru.rnemykin.gitlab.prtbot.service.client.gitlab.GitLabServiceClient;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserStorage {
    private static List<Integer> userIds;

    private final GitLabServiceClient gitLabClient;
    private final CheckPullRequestProperties checkPullRequestProperties;


    @EventListener(ApplicationReadyEvent.class)
    public void afterStart() {
        userIds = !CollectionUtils.isEmpty(checkPullRequestProperties.getUserIds())
                ? checkPullRequestProperties.getUserIds()
                : checkPullRequestProperties.getUserNames()
                .stream()
                .map(gitLabClient::findByName)
                .filter(Objects::nonNull)
                .map(User::getId)
                .collect(Collectors.toList());
    }

    public List<Integer> getUserIds() {
        return Collections.unmodifiableList(userIds);
    }
}
