package ru.rnemykin.gitlab.prtbot.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RegularMessageRepository extends JpaRepository<RegularMessage, UUID> {
    List<RegularMessage> findAllByCreateDateBefore(LocalDate date);
}
