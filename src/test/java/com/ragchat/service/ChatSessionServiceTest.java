package com.ragchat.service;

import com.ragchat.dto.SessionDto;
import com.ragchat.entity.ChatSession;
import com.ragchat.exception.ResourceNotFoundException;
import com.ragchat.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatSessionServiceTest {

    @Mock
    private ChatSessionRepository sessionRepository;

    @InjectMocks
    private ChatSessionService sessionService;

    private ChatSession mockSession;
    private final UUID SESSION_ID = UUID.randomUUID();
    private final String USER_ID = "user-123";

    @BeforeEach
    void setUp() {
        mockSession = ChatSession.builder()
                .id(SESSION_ID)
                .userId(USER_ID)
                .title("Test Session")
                .favorite(false)
                .deleted(false)
                .build();
    }

    @Test
    void createSession_shouldReturnResponse() {
        SessionDto.CreateRequest request = SessionDto.CreateRequest.builder()
                .userId(USER_ID).title("My Session").build();

        when(sessionRepository.save(any())).thenReturn(mockSession);

        SessionDto.Response response = sessionService.createSession(request);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        verify(sessionRepository, times(1)).save(any());
    }

    @Test
    void getSession_whenFound_shouldReturnResponse() {
        when(sessionRepository.findByIdAndUserIdAndDeletedFalse(SESSION_ID, USER_ID))
                .thenReturn(Optional.of(mockSession));

        SessionDto.Response response = sessionService.getSession(SESSION_ID, USER_ID);
        assertThat(response.getId()).isEqualTo(SESSION_ID);
    }

    @Test
    void getSession_whenNotFound_shouldThrow() {
        when(sessionRepository.findByIdAndUserIdAndDeletedFalse(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.getSession(SESSION_ID, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void toggleFavorite_shouldFlipFavoriteField() {
        when(sessionRepository.findByIdAndUserIdAndDeletedFalse(SESSION_ID, USER_ID))
                .thenReturn(Optional.of(mockSession));
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SessionDto.Response response = sessionService.toggleFavorite(SESSION_ID, USER_ID);
        assertThat(response.isFavorite()).isTrue(); // was false, now true
    }

    @Test
    void deleteSession_shouldMarkDeleted() {
        when(sessionRepository.findByIdAndUserIdAndDeletedFalse(SESSION_ID, USER_ID))
                .thenReturn(Optional.of(mockSession));
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sessionService.deleteSession(SESSION_ID, USER_ID);

        assertThat(mockSession.isDeleted()).isTrue();
        verify(sessionRepository).save(mockSession);
    }
}
