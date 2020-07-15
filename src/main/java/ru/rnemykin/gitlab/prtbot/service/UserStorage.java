package ru.rnemykin.gitlab.prtbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class UserStorage {
    private static List<Integer> userIds;

    private final GitLabServiceClient gitLabClient;
    private final CheckPullRequestProperties properties;


    @EventListener(ApplicationReadyEvent.class)
    public void afterStart() {
        userIds = !CollectionUtils.isEmpty(properties.getAuthorIds())
                ? properties.getAuthorIds()
                : properties.getUserNames()
                    .stream()
                    .map(gitLabClient::findByName)
                    .filter(Objects::nonNull)
                    .map(User::getId)
                    .collect(Collectors.toList());
    }

    public List<Integer> getUserIds() {
        log.info("userIds {}", userIds);
        return Collections.unmodifiableList(userIds);
    }
}
