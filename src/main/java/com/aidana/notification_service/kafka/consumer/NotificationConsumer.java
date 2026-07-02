package com.aidana.notification_service.kafka.consumer;

import com.aidana.notification_service.kafka.event.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationConsumer {

    @KafkaListener(topics = "transactions", groupId = "notification-service")
    public void consume(TransactionEvent event) {
        log.info("Received transaction event: {}", event);
    }
}
