package ru.rnemykin.gitlab.prtbot.service.job.strategy;

import org.gitlab4j.api.Constants.MergeRequestState;

public interface PullRequestProcessStrategy {
    void process();
    MergeRequestState type();
}
