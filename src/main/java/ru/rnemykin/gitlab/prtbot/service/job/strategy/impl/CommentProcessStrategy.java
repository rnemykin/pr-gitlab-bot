package ru.rnemykin.gitlab.prtbot.service.job.strategy.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.rnemykin.gitlab.prtbot.model.CommentMessage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentProcessStrategy extends AbstractCommentProcessStrategy<CommentMessage> {

    @Override
    protected Consumer<CommentMessage> action() {
        return this::post;
    }

    @Override
    protected List<CommentMessage> get() {
        List<CommentMessage> exist = commentMessageService.findAll();
        return getCommentsFromGitlab().stream().filter(c -> !exist.contains(c)).collect(Collectors.toList());
    }

    private List<CommentMessage> getCommentsFromGitlab() {
        return userStorage.getUserIds()
                .stream()
                .flatMap(id -> gitLabClient.findOpenedPullRequests(id)
                        .stream()
                        .map(pr -> gitLabClient.getComments(pr))
                        .flatMap(Collection::stream))
                .collect(Collectors.toList());
    }

    private void post(CommentMessage comment) {
        Optional<Message> result = telegramClient.newPrComment(comment);
        result.ifPresent(msg -> {
            commentMessageService.save(comment, msg);
            log.info("comment: \"{}\" was posted to telegram with id {}", comment.getText(), comment.getMessageId());
        });
    }
}
