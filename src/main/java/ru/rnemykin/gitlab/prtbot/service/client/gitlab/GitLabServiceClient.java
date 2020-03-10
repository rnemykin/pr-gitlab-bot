package ru.rnemykin.gitlab.prtbot.service.client.gitlab;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.Constants.MergeRequestState;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.MergeRequestFilter;
import org.gitlab4j.api.models.User;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GitLabServiceClient {
    private final GitLabApi apiClient;


    @SneakyThrows
    public User findByName(@NotEmpty String name) {
        return apiClient.getUserApi().getUser(name);
    }

    public List<MergeRequest> findOpenedPullRequests(int authorId) {
        return findPullRequests(authorId, MergeRequestState.OPENED);
    }

    public List<MergeRequest> findMergedPullRequests(int authorId) {
        return findPullRequests(authorId, MergeRequestState.MERGED);
    }

    @SneakyThrows
    public List<MergeRequest> findPullRequests(int authorId, MergeRequestState state) {
        MergeRequestFilter filter = new MergeRequestFilter();
        filter.setAuthorId(authorId);
        filter.setState(state);
        filter.setScope(Constants.MergeRequestScope.ALL);
        return apiClient.getMergeRequestApi().getMergeRequests(filter);
    }

}
