package ru.rnemykin.gitlab.prtbot.service.job.strategy;

import org.gitlab4j.api.Constants;
import org.gitlab4j.api.Constants.MergeRequestState;
import org.springframework.stereotype.Component;
import ru.rnemykin.gitlab.prtbot.service.job.strategy.impl.ProcessTypeStrategy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Component
public class PullRequestProcessStrategyFactory {
    private final Map<MergeRequestState, ProcessTypeStrategy> strategies;

    public PullRequestProcessStrategyFactory(List<ProcessTypeStrategy<Constants.MergeRequestState>> strategies) {
        this.strategies = strategies.stream().collect(toMap(ProcessTypeStrategy::type, Function.identity()));
    }

    public ProcessTypeStrategy get(MergeRequestState type) {
        return Optional
                .ofNullable(strategies.get(type))
                .orElseThrow(() -> new IllegalArgumentException("unknown type " + type));
    }
}
