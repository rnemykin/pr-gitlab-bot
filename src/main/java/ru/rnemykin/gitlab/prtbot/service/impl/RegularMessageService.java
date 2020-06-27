package ru.rnemykin.gitlab.prtbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rnemykin.gitlab.prtbot.model.RegularMessage;
import ru.rnemykin.gitlab.prtbot.model.RegularMessageRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegularMessageService extends AbstractEntityService<RegularMessage, RegularMessageRepository> {
    @Transactional(readOnly = true)
    public List<RegularMessage> findEarlerDate(LocalDate date) {
        return repository.findAllByCreateDateBefore(date);
    }

}
