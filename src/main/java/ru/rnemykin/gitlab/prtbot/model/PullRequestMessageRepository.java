package ru.rnemykin.gitlab.prtbot.model;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PullRequestMessageRepository extends JpaRepository<PullRequestMessage, UUID>, JpaSpecificationExecutor<PullRequestMessage> {
    class Specifications {
        public static Specification<PullRequestMessage> findByPullRequestId(Integer pullRequestId) {
            return (root, query, builder) -> builder.equal(root.get("pullRequestId"), pullRequestId);
        }
    }
}
