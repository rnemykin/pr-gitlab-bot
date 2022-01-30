package ru.rnemykin.gitlab.prtbot.service.job.strategy.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.rnemykin.gitlab.prtbot.service.impl.CommentMessageService;

public abstract class AbstractCommentProcessStrategy<E> extends AbstractProcessStrategy<E> {

    @Autowired
    protected CommentMessageService commentMessageService;
}

