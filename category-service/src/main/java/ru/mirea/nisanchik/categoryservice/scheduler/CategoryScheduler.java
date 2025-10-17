package ru.mirea.nisanchik.categoryservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.mirea.nisanchik.categoryservice.controller.kafka.producer.CategoryProducerHandler;
import ru.mirea.nisanchik.categoryservice.repository.OutboxRepository;

@Slf4j
@EnableScheduling
@Component
@RequiredArgsConstructor
public class CategoryScheduler {

    private final CategoryProducerHandler categoryProducerHandler;

    private final OutboxRepository outboxRepository;

    @Scheduled(cron = "0/30 * * * * *")
    public void schedule() {
        log.info("Scheduling Category Service");
        outboxRepository.findAll().forEach(categoryProducerHandler::send);
    }
}
