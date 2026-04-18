package com.finpulse.notes.dto;

import java.time.LocalDateTime;

public record NoteGroupResponse(
        String id,
        String name,
        String description,
        String color,
        String icon,
        int noteCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}