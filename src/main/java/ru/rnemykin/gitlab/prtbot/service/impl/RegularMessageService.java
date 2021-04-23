package ru.rnemykin.gitlab.prtbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.rnemykin.gitlab.prtbot.model.RegularMessage;
import ru.rnemykin.gitlab.prtbot.model.RegularMessageRepository;

@Service
@RequiredArgsConstructor
public class RegularMessageService extends AbstractEntityService<RegularMessage, RegularMessageRepository> {

}
