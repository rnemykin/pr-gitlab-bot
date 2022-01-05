package ru.rnemykin.gitlab.prtbot.service.job.strategy.impl;

import lombok.extern.log4j.Log4j2;
import org.gitlab4j.api.Constants.MergeRequestState;
import org.gitlab4j.api.models.MergeRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Log4j2
@Component
public class MergedPullRequestProcessStrategy extends AbstractPullRequestProcessStrategy<MergeRequest> {
    @Override
    protected Consumer<MergeRequest> action() {
        return pr -> prMessageService.findByPullRequestId(pr.getId()).ifPresent(msg -> {
            log.info("process PR{number: {}, status: {}}", pr.getIid(), pr.getMergeStatus());
            boolean success = telegramClient.deleteMessage(msg.getMessageId(), msg.getChatId());
            if (success) {
                log.info("delete PR{number: {}, status: {}}", pr.getIid(), pr.getMergeStatus());
                prMessageService.delete(msg);
            }
        });
    }

    @Override
    protected List<MergeRequest> get() {
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
