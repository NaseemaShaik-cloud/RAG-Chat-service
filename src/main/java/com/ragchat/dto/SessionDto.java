package com.ragchat.dto;

import com.ragchat.entity.ChatMessage.SenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

// ─── Session DTOs ───────────────────────────────────────────────────────────

public class SessionDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        @NotBlank(message = "userId is required")
        private String userId;

        @NotBlank(message = "title is required")
        private String title;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RenameRequest {
        @NotBlank(message = "title is required")
        private String title;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private UUID id;
        private String userId;
        private String title;
        private boolean favorite;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}

// ─── Message DTOs ────────────────────────────────────────────────────────────

class MessageDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        @NotNull(message = "sender is required")
        private SenderType sender;

        @NotBlank(message = "content is required")
        private String content;

        private String retrievedContext;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private UUID id;
        private UUID sessionId;
        private SenderType sender;
        private String content;
        private String retrievedContext;
        private LocalDateTime createdAt;
    }
}
