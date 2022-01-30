package ru.rnemykin.gitlab.prtbot.service.job.strategy.impl;

import org.gitlab4j.api.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import ru.rnemykin.gitlab.prtbot.service.impl.PullRequestMessageService;

public abstract class AbstractPullRequestProcessStrategy<E> extends AbstractProcessStrategy<E> implements ProcessTypeStrategy<Constants.MergeRequestState> {

    @Autowired
    protected PullRequestMessageService prMessageService;
}
