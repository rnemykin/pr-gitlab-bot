package ru.rnemykin.gitlab.prtbot.service.job.strategy;

import org.gitlab4j.api.Constants.MergeRequestState;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Component
public class PullRequestProcessStrategyFactory {
    private Map<MergeRequestState, PullRequestProcessStrategy> strategies;

    public PullRequestProcessStrategyFactory(List<PullRequestProcessStrategy> strategies) {
        this.strategies = strategies.stream().collect(toMap(PullRequestProcessStrategy::type, Function.identity()));
    }

    public PullRequestProcessStrategy get(MergeRequestState type) {
        return Optional
                .ofNullable(strategies.get(type))
                .orElseThrow(() -> new IllegalArgumentException("unknown type " + type));
    }
}
