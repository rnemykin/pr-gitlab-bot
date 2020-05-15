package ru.rnemykin.gitlab.prtbot.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegularMessageRepository extends CrudRepository<RegularMessage, String> {
}
