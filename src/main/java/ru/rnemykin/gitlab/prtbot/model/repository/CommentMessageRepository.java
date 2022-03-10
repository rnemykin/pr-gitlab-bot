package ru.rnemykin.gitlab.prtbot.model.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.rnemykin.gitlab.prtbot.model.CommentMessage;

import java.util.UUID;

@Repository
public interface CommentMessageRepository extends JpaRepository<CommentMessage, UUID>, JpaSpecificationExecutor<CommentMessage> {

    class Specifications {
        public static Specification<CommentMessage> findByNoteId(Integer noteId) {
            return (root, query, builder) -> builder.equal(root.get("noteId"), noteId);
        }
    }
}