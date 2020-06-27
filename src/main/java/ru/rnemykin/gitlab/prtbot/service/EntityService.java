package ru.rnemykin.gitlab.prtbot.service;

import java.util.List;
import java.util.UUID;

public interface EntityService<E> {
    E findById(UUID id);
    List<E> findAll();
    E save(E entity);
    void delete(E entity);
}
