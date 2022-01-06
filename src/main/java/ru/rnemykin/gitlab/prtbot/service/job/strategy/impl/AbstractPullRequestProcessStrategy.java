package ru.rnemykin.gitlab.prtbot.service.job.strategy.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.rnemykin.gitlab.prtbot.service.UserStorage;
import ru.rnemykin.gitlab.prtbot.service.client.gitlab.GitLabServiceClient;
import ru.rnemykin.gitlab.prtbot.service.client.telegram.TelegramServiceClient;
import ru.rnemykin.gitlab.prtbot.service.impl.PullRequestMessageService;
import ru.rnemykin.gitlab.prtbot.service.job.strategy.ProcessStrategy;

import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractPullRequestProcessStrategy<E> implements ProcessStrategy {
    @Autowired
    protected UserStorage userStorage;
    @Autowired
    protected GitLabServiceClient gitLabClient;
    @Autowired
    protected TelegramServiceClient telegramClient;
    @Autowired
    protected PullRequestMessageService prMessageService;

    protected abstract Consumer<E> action();
    protected abstract List<E> get();

    @Override
    public void process() {
        get().forEach(action());
    }
}
