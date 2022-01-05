package ru.rnemykin.gitlab.prtbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.rnemykin.gitlab.prtbot.model.RegularMessage;
import ru.rnemykin.gitlab.prtbot.model.repository.RegularMessageRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegularMessageService extends AbstractEntityService<RegularMessage, RegularMessageRepository> {

    public Optional<RegularMessage> findByPrId(@NotNull Integer pullRequestId) {
        return repository.findOne(RegularMessageRepository.Specifications.findByPullRequestId(pullRequestId));
    }
    public Optional<RegularMessage> findByMessageId(@NotNull Integer messageId) {
        return repository.findOne(RegularMessageRepository.Specifications.findByMessageId(messageId));
    }
}
