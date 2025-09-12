package com.loopers.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.service.DlqService;
import com.loopers.interfaces.consumer.support.DlqPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DlqEventHandler {

    private final DlqService dlqService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void handleDlqMessage(DlqPublisher.DlqMessage dlqMessage) {
        try {
            dlqService.saveDeadLetterEvent(
                dlqMessage.getOriginalTopic(),
                objectMapper.writeValueAsString(dlqMessage.getOriginalMessage()),
                dlqMessage.getConsumerName(),
                dlqMessage.getErrorMessage()
            );

        } catch (Exception e) {
            log.error("DLQ 메시지 처리 실패", e);
        }
    }
}
