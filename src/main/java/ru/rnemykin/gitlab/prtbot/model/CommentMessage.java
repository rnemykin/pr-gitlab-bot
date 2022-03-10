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
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentMessage that = (CommentMessage) o;
        return noteId.equals(that.noteId) && author.equals(that.author) && text.equals(that.text);
    }
}
