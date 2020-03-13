package ru.rnemykin.gitlab.prtbot.service.job;

import lombok.RequiredArgsConstructor;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.User;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.rnemykin.gitlab.prtbot.config.properties.CheckPullRequestProperties;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessage;
import ru.rnemykin.gitlab.prtbot.model.PullRequestUpdateMessage;
import ru.rnemykin.gitlab.prtbot.service.PrMessageService;
import ru.rnemykin.gitlab.prtbot.service.client.gitlab.GitLabServiceClient;
import ru.rnemykin.gitlab.prtbot.service.client.telegram.TelegramServiceClient;

import java.util.List;
import java.util.Map;
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
            gitLabClient.findOpenedPullRequests(userId).forEach(this::processOpenedPullRequest);
        }
    }

    private void processOpenedPullRequest(MergeRequest pr) {
        Optional<PullRequestMessage> found = prMessageService.findByPrId(pr.getId());
        if (found.isEmpty()) {
            Optional<Message> result = telegramServiceClient.newPrNotification(pr);
            result.ifPresent(msg -> prMessageService.createMessage(pr.getId(), msg.getMessageId(), msg.getChatId()));
        } else {
            PullRequestMessage prMessage = found.get();
            Map<String, Long> unresolvedThreadsMap = gitLabClient.getUnresolvedThreadsMap(pr.getProjectId(), pr.getIid());
            unresolvedThreadsMap.remove(pr.getAuthor().getName());

            telegramServiceClient.updatePrMessage(
                    PullRequestUpdateMessage.builder()
                            .request(pr)
                            .telegramChatId(prMessage.getChatId())
                            .telegramMessageId(prMessage.getMessageId())
                            .upVoterNames(gitLabClient.getUpVoterNames(pr.getProjectId(), pr.getIid()))
                            .unresolvedThreadsMap(unresolvedThreadsMap)
                            .build()
            );
        }
    }

    @Scheduled(cron = "${app.job.notifyAboutMergedPr}")
    public void notifyAboutMergedPr() {
        for (Integer userId : userIds) {
            gitLabClient.findMergedPullRequests(userId).forEach(this::processMergedPullRequest);
        }
    }

    private void processMergedPullRequest(MergeRequest pr) {
        prMessageService.findByPrId(pr.getId()).ifPresent(msg -> {
            boolean success = telegramServiceClient.deleteMessage(msg.getMessageId(), msg.getChatId());
            if (success) {
                prMessageService.archiveMessage(msg.getId());
            }
        });
    }
}
