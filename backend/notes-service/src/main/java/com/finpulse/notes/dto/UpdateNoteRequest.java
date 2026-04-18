package com.finpulse.notes.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateNoteRequest(
        @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
        String title,

        String content,

        String groupId,

        List<String> tags,

        String color,

        Boolean pinned,

        Boolean archived
) {}