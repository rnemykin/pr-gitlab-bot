package ru.rnemykin.gitlab.prtbot.web;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessage;
import ru.rnemykin.gitlab.prtbot.model.PullRequestMessageFilter;
import ru.rnemykin.gitlab.prtbot.service.impl.PullRequestMessageService;

@RestController
@RequestMapping("/api/v1/pull-request-messages")
@RequiredArgsConstructor
public class ApiController {
    private final PullRequestMessageService service;

    @GetMapping
    public Page<PullRequestMessage> findAll(PullRequestMessageFilter filter, Pageable page) {
        filter.setPage(page);
        return service.findAll(filter);
    }
}
