package ru.rnemykin.gitlab.prtbot.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@Table
@Entity
public class PullRequestMessage {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    private Long chatId;
    private Integer messageId;
    private Integer pullRequestId;
    private String pullRequestUrl;
}
