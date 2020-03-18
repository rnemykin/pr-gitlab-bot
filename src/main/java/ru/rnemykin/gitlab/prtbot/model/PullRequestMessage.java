package ru.rnemykin.gitlab.prtbot.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table
@Entity
public class PullRequestMessage {
    public enum Status {
        NEW, DELETED
    }

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    private Long chatId;
    private Integer messageId;
    private Integer pullRequestId;
    private Integer pullRequestNumber;

    @Enumerated(EnumType.STRING)
    private Status status = Status.NEW;
    private LocalDateTime deleteTime;
}
