package ru.rnemykin.gitlab.prtbot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.rnemykin.gitlab.prtbot.service.EntityService;

import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Transactional
public class AbstractEntityService<E, R extends JpaRepository<E, UUID>> implements EntityService<E> {
    @Autowired
    protected R repository;


    @Override
    @Transactional(readOnly = true)
    public E findById(@NotNull UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("entity not found, id = " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<E> findAll() {
        return repository.findAll();
    }

    @Override
    public E save(@NotNull E entity) {
        return repository.save(entity);
    }

    @Override
    public void delete(@NotNull E entity) {
        repository.delete(entity);
    }
}
