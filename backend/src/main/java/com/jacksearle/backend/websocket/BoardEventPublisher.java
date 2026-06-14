package com.jacksearle.backend.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class BoardEventPublisher {
    private final SimpMessagingTemplate messagingTemplate;

    public BoardEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(BoardEventType eventType, Long boardId, Long taskId, Map<String, Object> data) {
        BoardEvent event = new BoardEvent(eventType, boardId, taskId, Instant.now(), data);

        Runnable publisher = () -> messagingTemplate.convertAndSend("/topic/boards/" + boardId, event);
        publishAfterCommit(publisher);
    }

    public void publishToUser(String username, BoardEventType eventType, Long boardId, Long taskId, Map<String, Object> data) {
        BoardEvent event = new BoardEvent(eventType, boardId, taskId, Instant.now(), data);

        Runnable publisher = () -> messagingTemplate.convertAndSendToUser(username, "/queue/invites", event);
        publishAfterCommit(publisher);
    }

    private void publishAfterCommit(Runnable publisher) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publisher.run();
                }
            });
        } else {
            publisher.run();
        }
    }
}
