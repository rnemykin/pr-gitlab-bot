package ru.rnemykin.gitlab.prtbot.model.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.rnemykin.gitlab.prtbot.model.RegularMessage;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RegularMessageRepository extends JpaRepository<RegularMessage, UUID>, JpaSpecificationExecutor<RegularMessage> {
    List<RegularMessage> findAllByCreateDateBefore(LocalDate date);

    class Specifications {
        public static Specification<RegularMessage> findByPullRequestId(Integer pullRequestId) {
            return (root, query, builder) -> builder.equal(root.get("pullRequestId"), pullRequestId);
        }
    }
}
