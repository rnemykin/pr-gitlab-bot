package ru.rnemykin.gitlab.prtbot.model;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PullRequestMessageRepository extends JpaRepository<PullRequestMessage, UUID>, JpaSpecificationExecutor<PullRequestMessage> {
    class Specifications {
        public static Specification<PullRequestMessage> findByPullRequestId(Integer pullRequestId) {
            return (root, query, builder) -> builder.and(
                    builder.equal(root.get("pullRequestId"), pullRequestId),
                    builder.notEqual(root.get("status"), PullRequestMessage.Status.MERGED)
            );
        }

        public static Specification<PullRequestMessage> findNotMergedEarlierDate(LocalDateTime date) {
            return (root, query, builder) -> builder.and(
                    builder.lessThan(root.get("createDate"), builder.literal(date)),
                    builder.notEqual(root.get("status"), PullRequestMessage.Status.MERGED)
            );
        }
    }

    List<PullRequestMessage> findAllByCreateDateBefore(LocalDateTime date);
}
