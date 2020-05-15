package ru.rnemykin.gitlab.prtbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rnemykin.gitlab.prtbot.model.RegularMessage;
import ru.rnemykin.gitlab.prtbot.model.RegularMessageRepository;

import javax.validation.constraints.NotNull;

@Service
@Transactional
@RequiredArgsConstructor
public class RegularMessageService {
    private final RegularMessageRepository repository;


    public RegularMessage save(@NotNull RegularMessage msg) {
        return repository.save(msg);
    }

    public void deleteAll() {
        repository.deleteAll();
    }
}
