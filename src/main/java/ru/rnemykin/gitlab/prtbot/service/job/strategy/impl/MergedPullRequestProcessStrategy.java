package ru.rnemykin.gitlab.prtbot.service.job.strategy.impl;

import org.gitlab4j.api.Constants.MergeRequestState;
import org.gitlab4j.api.models.MergeRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class MergedPullRequestProcessStrategy extends AbstractPullRequestProcessStrategy {
    @Override
    protected Consumer<MergeRequest> action() {
        return pr -> prMessageService.findByPrId(pr.getId()).ifPresent(msg -> {
            boolean success = telegramClient.deleteMessage(msg.getMessageId(), msg.getChatId());
            if (success) {
                prMessageService.archiveMessage(msg.getId());
            }
        });
    }

    @Override
    protected List<MergeRequest> getMergeRequests() {
        return userStorage.getUserIds().stream()
                .map(gitLabClient::findCompletedPullRequests)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public MergeRequestState type() {
        return MergeRequestState.MERGED;
    }
}
