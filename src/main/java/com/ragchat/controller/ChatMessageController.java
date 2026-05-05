package com.ragchat.controller;

import com.ragchat.dto.ApiResponse;
import com.ragchat.dto.MessageDto;
import com.ragchat.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/messages")
@RequiredArgsConstructor
@Tag(name = "Chat Messages", description = "Manage messages within a session")
@SecurityRequirement(name = "ApiKeyAuth")
public class ChatMessageController {

    private final ChatMessageService messageService;

    @PostMapping
    @Operation(summary = "Add a message to a session")
    public ResponseEntity<ApiResponse<MessageDto.Response>> addMessage(
            @PathVariable UUID sessionId,
            @RequestParam String userId,
            @Valid @RequestBody MessageDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message saved",
                        messageService.addMessage(sessionId, userId, request)));
    }

    @GetMapping
    @Operation(summary = "Get paginated message history for a session")
    public ResponseEntity<ApiResponse<Page<MessageDto.Response>>> getMessages(
            @PathVariable UUID sessionId,
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                messageService.getMessages(sessionId, userId, pageable)));
    }
}
