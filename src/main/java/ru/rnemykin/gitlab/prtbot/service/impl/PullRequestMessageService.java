package ru.rnemykin.gitlab.prtbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.gitlab4j.api.models.MergeRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessage;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessageFilter;
import ru.rnemykin.gitlab.prtbot.model.repository.PullRequestMessageRepository;
import ru.rnemykin.gitlab.prtbot.model.repository.PullRequestMessageRepository.Specifications;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PullRequestMessageService extends DeletableEntityService<PullRequestMessage, PullRequestMessageRepository> {

    public PullRequestMessage createMessage(MergeRequest pr, Message msg, boolean isEmpty) {
        PullRequestMessage entity = new PullRequestMessage();
        entity.setChatId(msg.getChatId());
        entity.setMessageId(msg.getMessageId());
        entity.setPullRequestId(pr.getId());
        entity.setPullRequestUrl(pr.getWebUrl());
        entity.setPullRequestIid(pr.getIid());
        entity.setProjectId(pr.getProjectId());
        if (!isEmpty) {
            UUID id = findByPullRequestId(pr.getId()).map(PullRequestMessage::getId).orElseThrow();
            entity.setId(id);
            entity.setDeleted(false);
        }
        return repository.save(entity);
    }

    public Optional<PullRequestMessage> findByPullRequestUrl(@NotNull String pullRequestUrl) {
        return repository.findOne(Specifications.findByPullRequestUrl(pullRequestUrl));
    }

    public Optional<PullRequestMessage> findByPullRequestId(@NotNull Integer pullRequestId) {
        return repository.findOne(Specifications.findByPullRequestId(pullRequestId));
    }

    public Page<PullRequestMessage> findAll(PullRequestMessageFilter filter) {
        return repository.findAll(filter, filter.getPage());
    }

    @Override
    public void delete(PullRequestMessage message) {
        repository.markDeleted(message);
    }
}
