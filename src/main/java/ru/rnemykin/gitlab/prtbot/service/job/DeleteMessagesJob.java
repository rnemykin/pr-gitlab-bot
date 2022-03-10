package ru.rnemykin.gitlab.prtbot.service.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.rnemykin.gitlab.prtbot.model.AbstractMessage;
import ru.rnemykin.gitlab.prtbot.service.client.telegram.TelegramServiceClient;
import ru.rnemykin.gitlab.prtbot.service.impl.DeletableEntityService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteMessagesJob {
    private final TelegramServiceClient telegramServiceClient;
    private final List<DeletableEntityService> messageServices;

    @Autowired
    public DeleteMessagesJob(List<DeletableEntityService> messageServices, TelegramServiceClient telegramServiceClient) {
        this.messageServices = messageServices.stream().filter(DeletableEntityService::isDeletable).collect(Collectors.toList());
        this.telegramServiceClient = telegramServiceClient;
    }

    @Scheduled(cron = "${app.job.deleteRegularMessages}")
    public void deleteOldMessages() {
        messageServices.forEach(messageService -> {
                    List<? extends AbstractMessage> messages = messageService.findAll();
                    for (AbstractMessage message : messages) {
                        log.info("try to delete message id={}", message.getId());
                        if (telegramServiceClient.deleteMessage(message.getMessageId(), message.getChatId())) {
                            messageService.delete(message);
                        } else {
                            log.warn("can't delete message, id = {}", message.getId());
                        }
                    }
                }
        );
    }
}
