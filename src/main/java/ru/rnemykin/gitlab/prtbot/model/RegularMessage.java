package ru.rnemykin.gitlab.prtbot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Table
@Entity
public class RegularMessage extends AbstractMessage {
    private Integer pullRequestId;
}
