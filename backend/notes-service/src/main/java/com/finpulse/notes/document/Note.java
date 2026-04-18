package com.finpulse.notes.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
Diferenças entre MongoDB e JPA:

@Document em vez de @Entity — define a coleção no MongoDB
@Id é String em vez de UUID — MongoDB gera ObjectId automaticamente
@TextIndexed — cria um índice de texto completo (full-text search) no conteúdo da nota, permitindo busca por palavras
@CompoundIndex — índice composto para buscas que filtram por userId + groupId juntos (otimiza performance)
@CreatedDate / @LastModifiedDate — preenchidos automaticamente pelo Spring Data (precisamos habilitar isso)
*/
@Document(collection = "notes")
@CompoundIndex(name = "user_group_idx", def = "{'userId': 1, 'groupId': 1}")
public class Note {

    @Id
    private String id;

    private String title;

    @TextIndexed
    private String content;

    @Indexed
    private String groupId;

    @Indexed
    private String userId;

    private List<String> tags = new ArrayList<>();

    private String color;

    private boolean pinned = false;

    private boolean archived = false;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Note() {
    }

    public Note(String title, String content, String groupId, String userId) {
        this.title = title;
        this.content = content;
        this.groupId = groupId;
        this.userId = userId;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}