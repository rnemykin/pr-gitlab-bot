package ru.rnemykin.gitlab.prtbot.service.job.strategy.impl;

import org.gitlab4j.api.Constants.MergeRequestState;
import org.gitlab4j.api.models.MergeRequest;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessage;
import ru.rnemykin.gitlab.prtbot.model.PullRequestUpdateMessage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class OpenedPullRequestProcessStrategy extends AbstractPullRequestProcessStrategy {
    @Override
    protected Consumer<MergeRequest> action() {
        return pr -> {
            Optional<PullRequestMessage> found = prMessageService.findByPrId(pr.getId());
            if (found.isEmpty()) {
                Optional<Message> result = telegramClient.newPrNotification(pr);
                result.ifPresent(msg -> prMessageService.createMessage(pr.getId(), msg.getMessageId(), msg.getChatId()));
                return;
            }

            PullRequestMessage prMessage = found.get();
            Map<String, Long> unresolvedThreadsMap = gitLabClient.getUnresolvedThreadsMap(pr.getProjectId(), pr.getIid());
            unresolvedThreadsMap.remove(pr.getAuthor().getName());

            telegramClient.updatePrMessage(
                    PullRequestUpdateMessage.builder()
                            .request(pr)
                            .telegramChatId(prMessage.getChatId())
                            .telegramMessageId(prMessage.getMessageId())
                            .upVoterNames(gitLabClient.getUpVoterNames(pr.getProjectId(), pr.getIid()))
                            .unresolvedThreadsMap(unresolvedThreadsMap)
                            .lastPipeline(gitLabClient.findLastPipeline(pr.getProjectId(), pr.getIid()).orElse(null))
                            .build()
            );
        };
    }

    @Override
    protected List<MergeRequest> getMergeRequests() {
        return userStorage.getUserIds().stream()
                .map(gitLabClient::findOpenedPullRequests)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public MergeRequestState type() {
        return MergeRequestState.OPENED;
    }
}
