package ru.rnemykin.gitlab.prtbot.service.job.strategy;

public interface ProcessStrategy<T> {
    void process();

    T type();
}
