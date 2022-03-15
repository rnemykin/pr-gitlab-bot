package ru.rnemykin.gitlab.prtbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.rnemykin.gitlab.prtbot.model.CommentMessage;
import ru.rnemykin.gitlab.prtbot.model.repository.CommentMessageRepository;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentMessageService extends AbstractEntityService<CommentMessage, CommentMessageRepository> {

    public void save(CommentMessage comment, Message msg) {
        comment.setChatId(msg.getChatId());
        comment.setMessageId(msg.getMessageId());
        repository.save(comment);
    }

    public Optional<CommentMessage> findByNoteId(@NotNull Integer noteId) {
        return repository.findOne(CommentMessageRepository.Specifications.findByNoteId(noteId));
    }

}