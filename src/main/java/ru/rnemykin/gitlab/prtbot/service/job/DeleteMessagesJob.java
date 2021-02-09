package ru.rnemykin.gitlab.prtbot.service.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.rnemykin.gitlab.prtbot.config.properties.CheckPullRequestProperties;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessage;
import ru.rnemykin.gitlab.prtbot.model.RegularMessage;
import ru.rnemykin.gitlab.prtbot.service.client.telegram.TelegramServiceClient;
import ru.rnemykin.gitlab.prtbot.service.impl.PullRequestMessageService;
import ru.rnemykin.gitlab.prtbot.service.impl.RegularMessageService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteMessagesJob {
    private final CheckPullRequestProperties prProperties;
    private final TelegramServiceClient telegramServiceClient;
    private final RegularMessageService regularMessageService;
    private final PullRequestMessageService pullRequestMessageService;


    @Scheduled(cron = "${app.job.deleteMessages}")
    public void deleteOldMessages() {
        LocalDateTime targetDate = LocalDateTime.now().minusDays(prProperties.getRegularMessagesTtl().toHours());
        List<RegularMessage> messages = regularMessageService.findEarlierDate(targetDate);
        for (RegularMessage message : messages) {
            log.info("try to delete regularMessage id={}", message.getId());
            if(telegramServiceClient.deleteMessage(message.getMessageId(), message.getChatId())) {
                regularMessageService.delete(message);
            } else {
                log.warn("can't delete regular message, id = {}", message.getId());
            }
        }

        List<PullRequestMessage> prMessages = pullRequestMessageService.findEarlierDate(targetDate);
    }
}
