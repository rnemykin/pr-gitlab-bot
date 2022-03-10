package ru.rnemykin.gitlab.prtbot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Table
@Entity
public class PullRequestMessage extends AbstractMessage {
    private Integer pullRequestId;
    private String pullRequestUrl;
    private Integer pullRequestIid;
    private Integer projectId;
    private boolean isDeleted;
}
