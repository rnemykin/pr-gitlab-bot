package ru.rnemykin.gitlab.prtbot.service.client.gitlab;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.Constants.MergeRequestState;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Discussion;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.MergeRequestFilter;
import org.gitlab4j.api.models.Note;
import org.gitlab4j.api.models.Pipeline;
import org.gitlab4j.api.models.User;
import org.springframework.stereotype.Component;
import ru.rnemykin.gitlab.prtbot.config.properties.CheckPullRequestProperties;
import ru.rnemykin.gitlab.prtbot.config.properties.TelegramProperties;
import ru.rnemykin.gitlab.prtbot.model.AbstractMessage;
import ru.rnemykin.gitlab.prtbot.model.CommentMessage;
import ru.rnemykin.gitlab.prtbot.service.impl.CommentMessageService;
import ru.rnemykin.gitlab.prtbot.service.impl.RegularMessageService;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.ZoneId.systemDefault;
import static org.gitlab4j.api.models.Note.Type.DIFF_NOTE;
import static org.gitlab4j.api.models.Note.Type.DISCUSSION_NOTE;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitLabServiceClient {
    private final GitLabApi apiClient;
    private final CheckPullRequestProperties checkPrProperties;
    private final RegularMessageService regularMessageService;
    private final CommentMessageService commentMessageService;
    private final TelegramProperties telegramProperties;
    private static final List<Note.Type> ALLOWED_TYPES = List.of(DIFF_NOTE, DISCUSSION_NOTE);


    @SneakyThrows
    public User findByName(@NotEmpty String name) {
        return apiClient.getUserApi().getUser(name);
    }

    public List<MergeRequest> findOpenedPullRequests(int authorId) {
        return findPullRequests(authorId, MergeRequestState.OPENED);
    }

    public List<MergeRequest> findCompletedPullRequests(int authorId) {
        return Stream.concat(
                findPullRequests(authorId, MergeRequestState.MERGED).stream(),
                findPullRequests(authorId, MergeRequestState.CLOSED).stream()
        ).collect(Collectors.toList());
    }

    @SneakyThrows
    public String getDiscussionId(Integer projectId, Integer mergeRequestId) {
        return apiClient.getDiscussionsApi().getMergeRequestDiscussions(projectId, mergeRequestId)
                .stream()
                .map(Discussion::getId)
                .findFirst().orElse(null);
    }

    @SneakyThrows
    public Integer addComment(CommentMessage comment) {
        String discussionId = getDiscussionId(comment.getProjectId(), comment.getPullRequestIid());
        String text = comment.getText() + " | by " + comment.getAuthor();
        return apiClient.getDiscussionsApi().addMergeRequestThreadNote(
                        comment.getProjectId(),
                        comment.getPullRequestIid(),
                        discussionId,
                        text,
                        Date.from(comment.getCreatedAt().toInstant(ZoneOffset.UTC)))
                .getId();
    }

    @SneakyThrows
    public void deleteComment(CommentMessage comment) {
        String discussionId = getDiscussionId(comment.getProjectId(), comment.getPullRequestIid());

        apiClient.getDiscussionsApi().deleteMergeRequestThreadNote(
                comment.getProjectId(),
                comment.getPullRequestIid(),
                discussionId,
                comment.getNoteId());
    }

    @SneakyThrows
    private List<MergeRequest> findPullRequests(int authorId, MergeRequestState state) {
        MergeRequestFilter filter = new MergeRequestFilter();
        filter.setAuthorId(authorId);
        filter.setState(state);
        filter.setScope(Constants.MergeRequestScope.ALL);
        filter.setOrderBy(Constants.MergeRequestOrderBy.CREATED_AT);
        filter.setSort(Constants.SortOrder.ASC);

        if (checkPrProperties.getSkippPrDaysPassCount() != null) {
            LocalDateTime createAfterDate = LocalDate.now().minusDays(checkPrProperties.getSkippPrDaysPassCount().toDays()).atStartOfDay();
            filter.setCreatedAfter(Date.from(createAfterDate.atZone(systemDefault()).toInstant()));
        }
        return apiClient.getMergeRequestApi().getMergeRequests(filter);
    }

    @SneakyThrows
    public List<CommentMessage> getComments(MergeRequest pullRequest) {
        return apiClient.getDiscussionsApi().getMergeRequestDiscussions(pullRequest.getProjectId(), pullRequest.getIid())
                .stream()
                .map(Discussion::getNotes)
                .flatMap(Collection::stream)
                .filter(n -> n.getType() != null && ALLOWED_TYPES.contains(n.getType()) && n.getResolved() != null && !n.getResolved())
                .map(n -> toComment(n, pullRequest))
                .collect(Collectors.toList());
    }

    private CommentMessage toComment(Note note, MergeRequest pr) {
        CommentMessage comment = extractCommentData(note);
        setPullRequestData(comment, pr);

        comment.setSource(CommentMessage.Source.GITLAB);
        comment.setChatId(telegramProperties.getCommentsChatId());

        return comment;
    }

    private CommentMessage extractCommentData(Note note) {
        CommentMessage comment = commentMessageService.findByNoteId(note.getId()).orElse(new CommentMessage());
        comment.setIsProcessed(comment.equals(new CommentMessage()) ? Boolean.FALSE : Boolean.TRUE);
        comment.setNoteId(note.getId());
        comment.setAuthor(note.getAuthor().getName());
        comment.setText(note.getBody().split(" \\|", 2)[0]);
        comment.setCreatedAt(LocalDateTime.ofInstant(note.getCreatedAt().toInstant(), ZoneId.systemDefault()));

        return comment;
    }

    private void setPullRequestData(CommentMessage comment, MergeRequest pr) {
        comment.setPullRequestId(pr.getId());
        comment.setProjectId(pr.getProjectId());
        comment.setPullRequestIid(pr.getIid());

        Integer replyMessageId = regularMessageService.findByPrId(pr.getId()).map(AbstractMessage::getMessageId).orElseThrow();
        comment.setReplyMessageId(replyMessageId);
    }


    @SneakyThrows
    public Map<String, Long> getUnresolvedThreadsMap(int projectId, int pullRequestNumber) {
        return apiClient.getDiscussionsApi().getMergeRequestDiscussions(projectId, pullRequestNumber)
                .stream()
                .flatMap(d -> d.getNotes().stream())
                .filter(d -> Boolean.TRUE.equals(d.getResolvable()) && !Boolean.TRUE.equals(d.getResolved()))
                .collect(Collectors.groupingBy(n -> n.getAuthor().getName(), Collectors.counting()));
    }

    @SneakyThrows
    public Optional<Pipeline> findLastPipeline(int projectId, int pullRequestNumber) {
        return apiClient.getMergeRequestApi()
                .getMergeRequestPipelinesStream(projectId, pullRequestNumber)
                .max(Comparator.comparingInt(Pipeline::getId));
    }

    @SneakyThrows
    public List<String> getApproverNames(int projectId, int pullRequestNumber) {
        return apiClient.getMergeRequestApi()
                .getApprovals(projectId, pullRequestNumber)
                .getApprovedBy()
                .stream()
                .map(User::getName)
                .collect(Collectors.toList());
    }

}
