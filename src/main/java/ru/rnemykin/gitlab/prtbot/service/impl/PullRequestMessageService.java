package ru.rnemykin.gitlab.prtbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.gitlab4j.api.models.MergeRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessage;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessageFilter;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessageRepository;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessageRepository.Specifications;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PullRequestMessageService extends AbstractEntityService<PullRequestMessage, PullRequestMessageRepository> {
    public PullRequestMessage createMessage(MergeRequest pr, Message msg) {
        PullRequestMessage entity = new PullRequestMessage();
        entity.setStatus(PullRequestMessage.Status.NEW);
        entity.setChatId(msg.getChatId());
        entity.setMessageId(msg.getMessageId());
        entity.setPullRequestId(pr.getId());
        entity.setPullRequestUrl(pr.getWebUrl());
        return repository.save(entity);
    }

    public Optional<PullRequestMessage> findByPrId(@NotNull Integer pullRequestId) {
        return repository.findOne(Specifications.findByPullRequestId(pullRequestId));
    }

    public Page<PullRequestMessage> findAll(PullRequestMessageFilter filter) {
        return repository.findAll(filter, filter.getPage());
    }

    public List<PullRequestMessage> findEarlierDate(LocalDateTime date) {
        return repository.findAllByCreateDateBefore(date);
    }

}
