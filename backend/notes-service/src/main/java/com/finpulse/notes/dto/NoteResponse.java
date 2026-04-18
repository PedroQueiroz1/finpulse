package com.finpulse.notes.dto;

import java.time.LocalDateTime;
import java.util.List;

public record NoteResponse(
        String id,
        String title,
        String content,
        String groupId,
        String userId,
        List<String> tags,
        String color,
        boolean pinned,
        boolean archived,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}