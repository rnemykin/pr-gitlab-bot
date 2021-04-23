package ru.rnemykin.gitlab.prtbot.service.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.rnemykin.gitlab.prtbot.model.AbstractMessage;
import ru.rnemykin.gitlab.prtbot.service.EntityService;
import ru.rnemykin.gitlab.prtbot.service.client.telegram.TelegramServiceClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteMessagesJob {
    private final TelegramServiceClient telegramServiceClient;
    private final List<EntityService<AbstractMessage>> messageServices;


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
