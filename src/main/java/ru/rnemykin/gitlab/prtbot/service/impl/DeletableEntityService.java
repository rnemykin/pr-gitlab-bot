package ru.rnemykin.gitlab.prtbot.service.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rnemykin.gitlab.prtbot.service.Deletable;

import java.util.UUID;

public class DeletableEntityService<E, R extends JpaRepository<E, UUID>> extends AbstractEntityService<E, R> implements Deletable {
    @Override
    public boolean isDeletable() {
        return Boolean.TRUE;
    }
}
