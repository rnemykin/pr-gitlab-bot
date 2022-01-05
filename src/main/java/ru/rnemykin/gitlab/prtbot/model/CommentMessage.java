package ru.rnemykin.gitlab.prtbot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Table
@Entity
public class CommentMessage extends AbstractMessage {
    private Integer replyMessageId;
    private Integer projectId;
    private Integer pullRequestId;
    private Integer pullRequestIid;
    private Integer noteId;
    private String author;
    private String text;
    private Source source;
    private Boolean isProcessed;
    private LocalDateTime createdAt;

    public enum Source {
        GITLAB, TELEGRAM;
    }
}
