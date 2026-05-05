package com.ragchat.controller;

import com.ragchat.dto.ApiResponse;
import com.ragchat.dto.SessionDto;
import com.ragchat.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Tag(name = "Chat Sessions", description = "Manage chat sessions")
@SecurityRequirement(name = "ApiKeyAuth")
public class ChatSessionController {

    private final ChatSessionService sessionService;

    @PostMapping
    @Operation(summary = "Create a new chat session")
    public ResponseEntity<ApiResponse<SessionDto.Response>> createSession(
            @Valid @RequestBody SessionDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Session created", sessionService.createSession(request)));
    }

    @GetMapping
    @Operation(summary = "List sessions for a user (paginated)")
    public ResponseEntity<ApiResponse<Page<SessionDto.Response>>> getSessions(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(sessionService.getUserSessions(userId, pageable)));
    }

    @GetMapping("/favorites")
    @Operation(summary = "List favorite sessions for a user")
    public ResponseEntity<ApiResponse<Page<SessionDto.Response>>> getFavorites(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(sessionService.getFavoriteSessions(userId, pageable)));
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get a specific session")
    public ResponseEntity<ApiResponse<SessionDto.Response>> getSession(
            @PathVariable UUID sessionId,
            @RequestParam String userId) {
        return ResponseEntity.ok(ApiResponse.success(sessionService.getSession(sessionId, userId)));
    }

    @PatchMapping("/{sessionId}/rename")
    @Operation(summary = "Rename a session")
    public ResponseEntity<ApiResponse<SessionDto.Response>> renameSession(
            @PathVariable UUID sessionId,
            @RequestParam String userId,
            @Valid @RequestBody SessionDto.RenameRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Session renamed",
                sessionService.renameSession(sessionId, userId, request)));
    }

    @PatchMapping("/{sessionId}/favorite")
    @Operation(summary = "Toggle favorite status of a session")
    public ResponseEntity<ApiResponse<SessionDto.Response>> toggleFavorite(
            @PathVariable UUID sessionId,
            @RequestParam String userId) {
        return ResponseEntity.ok(ApiResponse.success("Favorite toggled",
                sessionService.toggleFavorite(sessionId, userId)));
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Delete a session (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @PathVariable UUID sessionId,
            @RequestParam String userId) {
        sessionService.deleteSession(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success("Session deleted", null));
    }
}
