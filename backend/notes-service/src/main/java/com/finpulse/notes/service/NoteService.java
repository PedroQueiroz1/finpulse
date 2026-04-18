package com.finpulse.notes.service;

import com.finpulse.notes.document.Note;
import com.finpulse.notes.document.NoteGroup;
import com.finpulse.notes.dto.*;
import com.finpulse.notes.exception.GroupNotFoundException;
import com.finpulse.notes.exception.NoteNotFoundException;
import com.finpulse.notes.repository.NoteGroupRepository;
import com.finpulse.notes.repository.NoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {

    private static final Logger log = LoggerFactory.getLogger(NoteService.class);

    private final NoteRepository noteRepository;
    private final NoteGroupRepository groupRepository;

    public NoteService(NoteRepository noteRepository, NoteGroupRepository groupRepository) {
        this.noteRepository = noteRepository;
        this.groupRepository = groupRepository;
    }

    // ============================================================
    // Operações de Grupos
    // ============================================================

    public NoteGroupResponse createGroup(CreateNoteGroupRequest request, String userId) {
        log.info("Criando grupo '{}' para usuário {}", request.name(), userId);

        NoteGroup group = new NoteGroup(
                request.name(),
                request.description(),
                request.color(),
                request.icon(),
                userId
        );

        NoteGroup saved = groupRepository.save(group);
        return toGroupResponse(saved);
    }

    public List<NoteGroupResponse> getGroupsByUser(String userId) {
        return groupRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(this::toGroupResponse)
                .toList();
    }

    public void deleteGroup(String groupId, String userId) {
        NoteGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        if (!group.getUserId().equals(userId)) {
            throw new GroupNotFoundException(groupId);
        }

        // Remove todas as notas do grupo
        List<Note> notes = noteRepository.findByUserIdAndGroupId(userId, groupId);
        noteRepository.deleteAll(notes);

        groupRepository.delete(group);
        log.info("Grupo {} deletado com {} notas", groupId, notes.size());
    }

    // ============================================================
    // Operações de Notas
    // ============================================================

    public NoteResponse createNote(CreateNoteRequest request, String userId) {
        log.info("Criando nota '{}' para usuário {}", request.title(), userId);

        // Valida se o grupo existe (se informado)
        if (request.groupId() != null && !request.groupId().isBlank()) {
            if (!groupRepository.existsById(request.groupId())) {
                throw new GroupNotFoundException(request.groupId());
            }
            // Incrementa contador de notas do grupo
            groupRepository.findById(request.groupId()).ifPresent(group -> {
                group.setNoteCount(group.getNoteCount() + 1);
                groupRepository.save(group);
            });
        }

        Note note = new Note(request.title(), request.content(), request.groupId(), userId);
        if (request.tags() != null) {
            note.setTags(request.tags());
        }
        if (request.color() != null) {
            note.setColor(request.color());
        }

        Note saved = noteRepository.save(note);
        return toNoteResponse(saved);
    }

    public List<NoteResponse> getNotesByUser(String userId) {
        return noteRepository
                .findByUserIdAndArchivedFalseOrderByPinnedDescCreatedAtDesc(userId)
                .stream()
                .map(this::toNoteResponse)
                .toList();
    }

    public Page<NoteResponse> getNotesByUserPaginated(String userId, Pageable pageable) {
        return noteRepository.findActiveNotesByUserId(userId, pageable)
                .map(this::toNoteResponse);
    }

    public NoteResponse getNoteById(String id, String userId) {
        Note note = findNoteByIdAndUser(id, userId);
        return toNoteResponse(note);
    }

    public NoteResponse updateNote(String id, UpdateNoteRequest request, String userId) {
        Note note = findNoteByIdAndUser(id, userId);

        if (request.title() != null) note.setTitle(request.title());
        if (request.content() != null) note.setContent(request.content());
        if (request.groupId() != null) note.setGroupId(request.groupId());
        if (request.tags() != null) note.setTags(request.tags());
        if (request.color() != null) note.setColor(request.color());
        if (request.pinned() != null) note.setPinned(request.pinned());
        if (request.archived() != null) note.setArchived(request.archived());

        Note updated = noteRepository.save(note);
        log.info("Nota {} atualizada", id);
        return toNoteResponse(updated);
    }

    public void deleteNote(String id, String userId) {
        Note note = findNoteByIdAndUser(id, userId);

        // Decrementa contador do grupo
        if (note.getGroupId() != null) {
            groupRepository.findById(note.getGroupId()).ifPresent(group -> {
                group.setNoteCount(Math.max(0, group.getNoteCount() - 1));
                groupRepository.save(group);
            });
        }

        noteRepository.delete(note);
        log.info("Nota {} deletada", id);
    }

    public List<NoteResponse> searchNotes(String userId, String searchTerm) {
        return noteRepository.searchByContent(userId, searchTerm).stream()
                .map(this::toNoteResponse)
                .toList();
    }

    public NoteResponse togglePin(String id, String userId) {
        Note note = findNoteByIdAndUser(id, userId);
        note.setPinned(!note.isPinned());
        Note updated = noteRepository.save(note);
        return toNoteResponse(updated);
    }

    public NoteResponse archiveNote(String id, String userId) {
        Note note = findNoteByIdAndUser(id, userId);
        note.setArchived(true);
        Note updated = noteRepository.save(note);
        return toNoteResponse(updated);
    }

    // ============================================================
    // Métodos privados
    // ============================================================

    private Note findNoteByIdAndUser(String id, String userId) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));
        if (!note.getUserId().equals(userId)) {
            throw new NoteNotFoundException(id);
        }
        return note;
    }

    private NoteResponse toNoteResponse(Note note) {
        return new NoteResponse(
                note.getId(), note.getTitle(), note.getContent(),
                note.getGroupId(), note.getUserId(), note.getTags(),
                note.getColor(), note.isPinned(), note.isArchived(),
                note.getCreatedAt(), note.getUpdatedAt()
        );
    }

    private NoteGroupResponse toGroupResponse(NoteGroup group) {
        return new NoteGroupResponse(
                group.getId(), group.getName(), group.getDescription(),
                group.getColor(), group.getIcon(), group.getNoteCount(),
                group.getCreatedAt(), group.getUpdatedAt()
        );
    }
}