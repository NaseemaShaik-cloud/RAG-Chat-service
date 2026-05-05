package com.ragchat.service;

import com.ragchat.dto.MessageDto;
import com.ragchat.entity.ChatMessage;
import com.ragchat.entity.ChatSession;
import com.ragchat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

    private final ChatMessageRepository messageRepository;
    private final ChatSessionService sessionService;

    @Transactional
    public MessageDto.Response addMessage(UUID sessionId, String userId,
                                          MessageDto.CreateRequest request) {
        log.info("Adding message to session {} by user {}", sessionId, userId);
        ChatSession session = sessionService.findSessionOrThrow(sessionId, userId);

        ChatMessage message = ChatMessage.builder()
                .session(session)
                .sender(request.getSender())
                .content(request.getContent())
                .retrievedContext(request.getRetrievedContext())
                .build();

        return toResponse(messageRepository.save(message));
    }

    public Page<MessageDto.Response> getMessages(UUID sessionId, String userId, Pageable pageable) {
        // Validate ownership
        sessionService.findSessionOrThrow(sessionId, userId);
        return messageRepository
                .findBySessionIdOrderByCreatedAtAsc(sessionId, pageable)
                .map(this::toResponse);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private MessageDto.Response toResponse(ChatMessage m) {
        return MessageDto.Response.builder()
                .id(m.getId())
                .sessionId(m.getSession().getId())
                .sender(m.getSender())
                .content(m.getContent())
                .retrievedContext(m.getRetrievedContext())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
