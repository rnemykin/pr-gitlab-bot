package ru.rnemykin.gitlab.prtbot.service.client.gitlab;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.Constants.MergeRequestState;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.AbstractUser;
import org.gitlab4j.api.models.AwardEmoji;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.MergeRequestFilter;
import org.gitlab4j.api.models.Pipeline;
import org.gitlab4j.api.models.User;
import org.springframework.stereotype.Component;
import ru.rnemykin.gitlab.prtbot.config.properties.CheckPullRequestProperties;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.ZoneId.systemDefault;

@Component
@RequiredArgsConstructor
public class GitLabServiceClient {
    private static final String UPVOTE_EMOJI_ID = "thumbsup";
    private final GitLabApi apiClient;
    private final CheckPullRequestProperties checkPrProperties;


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
    private List<MergeRequest> findPullRequests(int authorId, MergeRequestState state) {
        MergeRequestFilter filter = new MergeRequestFilter();
        filter.setAuthorId(authorId);
        filter.setState(state);
        filter.setScope(Constants.MergeRequestScope.ALL);
        filter.setOrderBy(Constants.MergeRequestOrderBy.CREATED_AT);
        filter.setSort(Constants.SortOrder.ASC);

        if(checkPrProperties.getSkippPrDaysPassCount() != null) {
            LocalDateTime createAfterDate = LocalDate.now().minusDays(checkPrProperties.getSkippPrDaysPassCount().toDays()).atStartOfDay();
            filter.setCreatedAfter(Date.from(createAfterDate.atZone(systemDefault()).toInstant()));
        }
        return apiClient.getMergeRequestApi().getMergeRequests(filter);
    }

    @SneakyThrows
    public Map<String, Long> getUnresolvedThreadsMap(int projectId, int pullRequestNumber) {
        return apiClient.getDiscussionsApi().getMergeRequestDiscussions(projectId, pullRequestNumber)
                .stream()
                .flatMap(d -> d.getNotes().stream())
                .filter(d-> Boolean.TRUE.equals(d.getResolvable()) && !Boolean.TRUE.equals(d.getResolved()))
                .collect(Collectors.groupingBy(n -> n.getAuthor().getName(), Collectors.counting()));
    }

    @SneakyThrows
    public List<String> getUpVoterNames(int projectId, int pullRequestNumber) {
        return apiClient.getAwardEmojiApi().getMergeRequestAwardEmojis(projectId, pullRequestNumber)
                .stream()
                .filter(e -> UPVOTE_EMOJI_ID.equals(e.getName()))
                .map(AwardEmoji::getUser)
                .map(AbstractUser::getName)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public Optional<Pipeline> findLastPipeline(int projectId, int pullRequestNumber) {
        return apiClient.getMergeRequestApi()
                .getMergeRequestPipelinesStream(projectId, pullRequestNumber)
                .max(Comparator.comparingInt(Pipeline::getId));
    }

}
