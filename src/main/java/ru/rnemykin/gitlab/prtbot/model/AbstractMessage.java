package ru.rnemykin.gitlab.prtbot.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public class AbstractMessage {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    private Long chatId;
    private Integer messageId;
    private LocalDate createDate = LocalDate.now();
}
