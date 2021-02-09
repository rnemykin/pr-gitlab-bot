package ru.rnemykin.gitlab.prtbot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Data
@Table
@Entity
public class PullRequestMessage extends AbstractMessage {
    private Integer pullRequestId;
    private String pullRequestUrl;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        NEW,
        MERGED,
        DELETED
    }
}
