package ru.mirea.newrav1k.transactionservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.newrav1k.transactionservice.model.entity.ProcessedEvent;
import ru.mirea.newrav1k.transactionservice.repository.ProcessedEventRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcessedEventService {

    private final ProcessedEventRepository processedEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markEventAsProcessed(final ProcessedEvent processedEvent) {
        log.debug("Marking processed event {}", processedEvent);
        this.processedEventRepository.save(processedEvent);
    }

}