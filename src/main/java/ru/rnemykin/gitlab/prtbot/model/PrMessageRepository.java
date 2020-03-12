package ru.rnemykin.gitlab.prtbot.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PrMessageRepository extends JpaRepository<PullRequestMessage, UUID> {
}
