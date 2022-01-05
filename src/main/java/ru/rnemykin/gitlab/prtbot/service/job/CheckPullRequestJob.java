package ru.rnemykin.gitlab.prtbot.service.job;

import lombok.RequiredArgsConstructor;
import org.gitlab4j.api.Constants.MergeRequestState;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.rnemykin.gitlab.prtbot.service.job.strategy.PullRequestProcessStrategyFactory;
import ru.rnemykin.gitlab.prtbot.service.job.strategy.impl.CommentProcessStrategy;

@Component
@RequiredArgsConstructor
public class CheckPullRequestJob {
    private final PullRequestProcessStrategyFactory prProcessStrategyFactory;
    private final CommentProcessStrategy commentsProcessStrategy;


    @Scheduled(cron = "${app.job.notifyAboutOpenedPr}")
    public void notifyAboutOpenedPr() {
        prProcessStrategyFactory.get(MergeRequestState.OPENED).process();
        commentsProcessStrategy.process();
    }

    @Scheduled(cron = "${app.job.notifyAboutMergedPr}")
    public void notifyAboutMergedPr() {
        prProcessStrategyFactory.get(MergeRequestState.MERGED).process();
    }

}
