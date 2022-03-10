package ru.rnemykin.gitlab.prtbot.service.job.strategy.impl;

import ru.rnemykin.gitlab.prtbot.service.job.strategy.ProcessStrategy;

public interface ProcessTypeStrategy<T> extends ProcessStrategy {

    T type();
}
