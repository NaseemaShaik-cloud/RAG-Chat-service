package com.ragchat.repository;

import com.ragchat.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    Page<ChatSession> findByUserIdAndDeletedFalse(String userId, Pageable pageable);

    Page<ChatSession> findByUserIdAndFavoriteTrueAndDeletedFalse(String userId, Pageable pageable);

    Optional<ChatSession> findByIdAndUserIdAndDeletedFalse(UUID id, String userId);
}
