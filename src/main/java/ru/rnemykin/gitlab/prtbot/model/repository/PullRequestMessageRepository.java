package ru.rnemykin.gitlab.prtbot.model.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessage;

import java.util.UUID;

@Repository
public interface PullRequestMessageRepository extends JpaRepository<PullRequestMessage, UUID>, JpaSpecificationExecutor<PullRequestMessage> {

    default void markDeleted(PullRequestMessage message) {
        message.setDeleted(Boolean.TRUE);
        save(message);
    }

    class Specifications {
        public static Specification<PullRequestMessage> findByPullRequestUrl(String pullRequestUrl) {
            return (root, query, builder) -> builder.equal(root.get("pullRequestUrl"), pullRequestUrl);
        }
        public static Specification<PullRequestMessage> findByPullRequestId(Integer pullRequestId) {
            return (root, query, builder) -> builder.equal(root.get("pullRequestId"), pullRequestId);
        }
    }
}
