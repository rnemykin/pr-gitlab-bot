package ru.rnemykin.gitlab.prtbot.service.job.strategy.impl;

import org.gitlab4j.api.models.MergeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import ru.rnemykin.gitlab.prtbot.service.UserStorage;
import ru.rnemykin.gitlab.prtbot.service.client.gitlab.GitLabServiceClient;
import ru.rnemykin.gitlab.prtbot.service.client.telegram.TelegramServiceClient;
import ru.rnemykin.gitlab.prtbot.service.impl.PullRequestMessageService;
import ru.rnemykin.gitlab.prtbot.service.job.strategy.PullRequestProcessStrategy;

import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractPullRequestProcessStrategy implements PullRequestProcessStrategy {
    @Autowired
    protected UserStorage userStorage;
    @Autowired
    protected  GitLabServiceClient gitLabClient;
    @Autowired
    protected TelegramServiceClient telegramClient;
    @Autowired
    protected PullRequestMessageService prMessageService;

    protected abstract Consumer<MergeRequest> action();
    protected abstract List<MergeRequest> getMergeRequests();

    @Override
    public void process() {
        getMergeRequests().forEach(action());
    }
}
