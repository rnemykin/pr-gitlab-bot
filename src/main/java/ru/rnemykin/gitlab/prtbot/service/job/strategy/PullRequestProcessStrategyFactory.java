package ru.rnemykin.gitlab.prtbot.service.job.strategy;

import org.gitlab4j.api.Constants;
import org.gitlab4j.api.Constants.MergeRequestState;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Component
public class PullRequestProcessStrategyFactory {
    private final Map<MergeRequestState, ProcessStrategy<Constants.MergeRequestState>> strategies;

    public PullRequestProcessStrategyFactory(List<ProcessStrategy<Constants.MergeRequestState>> strategies) {
        this.strategies = strategies.stream().collect(toMap(ProcessStrategy::type, Function.identity()));
    }

    public ProcessStrategy<Constants.MergeRequestState> get(MergeRequestState type) {
        return Optional
                .ofNullable(strategies.get(type))
                .orElseThrow(() -> new IllegalArgumentException("unknown type " + type));
    }
}
