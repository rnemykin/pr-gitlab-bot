package ru.rnemykin.gitlab.prtbot.service.job.strategy.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.rnemykin.gitlab.prtbot.model.CommentMessage;
import ru.rnemykin.gitlab.prtbot.service.impl.CommentMessageService;

import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentProcessStrategy extends AbstractPullRequestProcessStrategy<CommentMessage> {

    private final CommentMessageService commentMessageService;

    @Override
    public Object type() {
        return null;
    }

    @Override
    protected Consumer<CommentMessage> action() {
        return comment -> {
            if (comment.getIsProcessed()) {
                delete(comment);
            } else {
                post(comment);
            }
        };
    }

    @Override
    protected List<CommentMessage> get() {
        Map<Integer, CommentMessage> gitlabCommentsMap = getCommentsFromGitlab().stream().collect(toMap(this::hashcode, Function.identity()));
        Map<Integer, CommentMessage> telegramCommentsMap = getTelegramComments().stream().collect(toMap(this::hashcode, Function.identity(), (a1, a2) -> a1));

        return findChanged(telegramCommentsMap, gitlabCommentsMap);
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

    private List<CommentMessage> getTelegramComments() {
        List<CommentMessage> telegramComments = commentMessageService.findAll();
        telegramComments.removeAll(filterNonExist(telegramComments));
        return telegramComments;
    }

    public List<CommentMessage> filterNonExist(List<CommentMessage> comments) {
        return comments.stream().filter(c -> !telegramClient.exist(c)).collect(Collectors.toList());
    }

    private void post(CommentMessage comment) {
        if (comment.getSource().equals(CommentMessage.Source.GITLAB)) {
            Optional<Message> result = telegramClient.newPrComment(comment);
            result.ifPresent(msg -> {
                commentMessageService.save(comment, msg);
                log.info("comment: \"{}\" was posted to telegram with id {}", comment.getText(), comment.getMessageId());
            });
        } else {
            Integer noteId = gitLabClient.addComment(comment);
            commentMessageService.save(comment, noteId);
            log.info("comment: \"{}\" was posted to pull request with id {}", comment.getText(), comment.getPullRequestIid());
        }
    }

    private void delete(CommentMessage comment) {
        log.info("try to delete comment \"{}\" with id={}", comment.getText(), comment.getMessageId());
        try {
            telegramClient.deleteMessage(comment.getMessageId(), comment.getChatId());
            gitLabClient.deleteComment(comment);
        } finally {
            commentMessageService.delete(comment);
            log.info("comment: \"{}\" was deleted with id {}", comment.getText(), comment.getId());
        }
    }

    private int hashcode(CommentMessage original) {
        CommentMessage comment = new CommentMessage();
        comment.setPullRequestIid(original.getPullRequestIid());
        comment.setPullRequestId(original.getPullRequestId());
        comment.setProjectId(original.getProjectId());
        comment.setText(original.getText());
        return comment.hashCode();
    }

    private List<CommentMessage> findChanged(Map<Integer, CommentMessage> map1, Map<Integer, CommentMessage> map2) {
        List<Map<Integer, CommentMessage>> commentMaps = orderBySize(map1, map2);

        return commentMaps.get(0).entrySet()
                .stream()
                .filter(g -> !equals(g, commentMaps.get(1)))
                .map(Map.Entry::getValue)
                .sorted(Comparator.comparingLong(c -> c.getCreatedAt().toEpochSecond(ZoneOffset.UTC)))
                .collect(Collectors.toList());
    }

    private List<Map<Integer, CommentMessage>> orderBySize(Map<Integer, CommentMessage> map1, Map<Integer, CommentMessage> map2) {
        return Stream.of(map1, map2).sorted(reverse(Comparator.comparingInt(Map::size))).collect(Collectors.toList());
    }

    private static <T> Comparator<T> reverse(Comparator<T> comparator) {
        return comparator.reversed();
    }

    private boolean equals(Map.Entry<Integer, CommentMessage> comment, Map<Integer, CommentMessage> comments) {
        return comments.get(comment.getKey()) != null && comment.getValue().getPullRequestIid().equals(comments.get(comment.getKey()).getPullRequestIid());
    }
}
