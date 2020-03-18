package ru.rnemykin.gitlab.prtbot.model;

import lombok.Data;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Data
public class PullRequestMessageFilter implements Specification<PullRequestMessage> {
    private String pullRequestNumber;
    private Pageable page;

    @Override
    public Predicate toPredicate(Root<PullRequestMessage> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        return StringUtils.hasText(pullRequestNumber)
                ? builder.like(root.get("pullRequestUrl"), pullRequestNumber)
                : builder.conjunction();
    }
}
