package ru.rnemykin.gitlab.prtbot.service.job;

import lombok.RequiredArgsConstructor;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.User;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.rnemykin.gitlab.prtbot.config.properties.CheckPullRequestProperties;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessage;
import ru.rnemykin.gitlab.prtbot.service.PrMessageService;
import ru.rnemykin.gitlab.prtbot.service.client.gitlab.GitLabServiceClient;
import ru.rnemykin.gitlab.prtbot.service.client.telegram.TelegramServiceClient;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CheckPullRequestJob {
    private static List<Integer> userIds;

    private final GitLabServiceClient gitLabClient;
    private final PrMessageService prMessageService;
    private final TelegramServiceClient telegramServiceClient;
    private final CheckPullRequestProperties checkPullRequestProperties;


    @EventListener(ApplicationStartedEvent.class)
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

    @Scheduled(cron = "${app.job.notifyAboutOpenedPr}")
    public void notifyAboutOpenedPr() {
        for (Integer userId : userIds) {
            List<MergeRequest> openedPullRequests = gitLabClient.findOpenedPullRequests(userId);
            for (MergeRequest pr : openedPullRequests) {
                Optional<PullRequestMessage> prMessage = prMessageService.findByPrId(pr.getId());
                if (prMessage.isEmpty()) {
                    telegramServiceClient
                            .newPrNotification(pr)
                            .ifPresent(msg -> prMessageService.createMessage(pr.getId(), msg.getMessageId(), msg.getChatId()));
                } else {
                    telegramServiceClient.updatePrMessage(pr, prMessage.get());
                }
            }
        }
    }

    @Scheduled(cron = "${app.job.notifyAboutMergedPr}")
    public void notifyAboutMergedPr() {
        for (Integer userId : userIds) {
            List<MergeRequest> mergedPullRequests = gitLabClient.findMergedPullRequests(userId);
            for (MergeRequest pr : mergedPullRequests) {
                prMessageService.findByPrId(pr.getId()).ifPresent(msg -> {
                    if (telegramServiceClient.deleteMessage(msg.getMessageId(), msg.getChatId())) {
                        prMessageService.archiveMessage(msg.getId());
                    }
                });
            }
        }
    }
}
