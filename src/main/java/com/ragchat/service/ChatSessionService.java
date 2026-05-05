package com.ragchat.service;

import com.ragchat.dto.SessionDto;
import com.ragchat.entity.ChatSession;
import com.ragchat.exception.ResourceNotFoundException;
import com.ragchat.repository.ChatSessionRepository;
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
public class ChatSessionService {

    private final ChatSessionRepository sessionRepository;

    @Transactional
    public SessionDto.Response createSession(SessionDto.CreateRequest request) {
        log.info("Creating session for user: {}", request.getUserId());
        ChatSession session = ChatSession.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .favorite(false)
                .deleted(false)
                .build();
        return toResponse(sessionRepository.save(session));
    }

    public Page<SessionDto.Response> getUserSessions(String userId, Pageable pageable) {
        return sessionRepository.findByUserIdAndDeletedFalse(userId, pageable)
                .map(this::toResponse);
    }

    public Page<SessionDto.Response> getFavoriteSessions(String userId, Pageable pageable) {
        return sessionRepository.findByUserIdAndFavoriteTrueAndDeletedFalse(userId, pageable)
                .map(this::toResponse);
    }

    public SessionDto.Response getSession(UUID sessionId, String userId) {
        return toResponse(findSessionOrThrow(sessionId, userId));
    }

    @Transactional
    public SessionDto.Response renameSession(UUID sessionId, String userId, SessionDto.RenameRequest request) {
        log.info("Renaming session {} for user: {}", sessionId, userId);
        ChatSession session = findSessionOrThrow(sessionId, userId);
        session.setTitle(request.getTitle());
        return toResponse(sessionRepository.save(session));
    }

    @Transactional
    public SessionDto.Response toggleFavorite(UUID sessionId, String userId) {
        log.info("Toggling favorite for session {} for user: {}", sessionId, userId);
        ChatSession session = findSessionOrThrow(sessionId, userId);
        session.setFavorite(!session.isFavorite());
        return toResponse(sessionRepository.save(session));
    }

    @Transactional
    public void deleteSession(UUID sessionId, String userId) {
        log.info("Soft-deleting session {} for user: {}", sessionId, userId);
        ChatSession session = findSessionOrThrow(sessionId, userId);
        session.setDeleted(true);
        sessionRepository.save(session);
    }

    // ── Internal helper ───────────────────────────────────────────────────────

    public ChatSession findSessionOrThrow(UUID sessionId, String userId) {
        return sessionRepository.findByIdAndUserIdAndDeletedFalse(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found: " + sessionId));
    }

    private SessionDto.Response toResponse(ChatSession s) {
        return SessionDto.Response.builder()
                .id(s.getId())
                .userId(s.getUserId())
                .title(s.getTitle())
                .favorite(s.isFavorite())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
