package ru.rnemykin.gitlab.prtbot.service;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageStrategy {
    long MESSAGE_FROM_TELEGRAM_SENDER_ID = 777000L;

    void process(Message message);

    boolean isApplicable(long senderId);
}
