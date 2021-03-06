package ru.rnemykin.gitlab.prtbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.Pipeline;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PullRequestUpdateMessage {
    private long telegramChatId;
    private int telegramMessageId;
    private MergeRequest request;
    private List<String> approverNames;
    private Map<String, Long> unresolvedThreadsMap;
    private Pipeline lastPipeline;
}
