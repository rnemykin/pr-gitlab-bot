package ru.rnemykin.gitlab.prtbot.service.job.strategy.impl;

import org.gitlab4j.api.Constants.MergeRequestState;
import org.gitlab4j.api.models.MergeRequest;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessage;
import ru.rnemykin.gitlab.prtbot.model.PullRequestUpdateMessage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class OpenedPullRequestProcessStrategy extends AbstractPullRequestProcessStrategy<MergeRequest> {
    @Override
    protected Consumer<MergeRequest> action() {
        return pr -> {
            Optional<PullRequestMessage> found = prMessageService.findByPullRequestId(pr.getId());
            boolean isEmpty = found.isEmpty();
            if (isEmpty || found.get().isDeleted()) {
                Optional<Message> result = telegramClient.newPrNotification(pr, !isEmpty);
                result.ifPresent(msg -> prMessageService.createMessage(pr, msg, isEmpty));
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
                            .approverNames(gitLabClient.getApproverNames(pr.getProjectId(), pr.getIid()))
                            .unresolvedThreadsMap(unresolvedThreadsMap)
                            .lastPipeline(gitLabClient.findLastPipeline(pr.getProjectId(), pr.getIid()).orElse(null))
                            .build()
            );
        };
    }

    @Override
    protected List<MergeRequest> get() {
        return userStorage.getUserIds().stream()
                .map(gitLabClient::findOpenedPullRequests)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparingInt(MergeRequest::getIid))
                .collect(Collectors.toList());
    }

    @Override
    public MergeRequestState type() {
        return MergeRequestState.OPENED;
    }
}
