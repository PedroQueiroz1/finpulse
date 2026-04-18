package com.finpulse.notes.controller;

import com.finpulse.notes.dto.*;
import com.finpulse.notes.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
Note o @RequestHeader("X-User-Id") — por enquanto, o Notes Service recebe o ID do usuário via header. 
Quando implementarmos o API Gateway, ele vai extrair o userId do JWT e injetar esse header automaticamente. 
Isso mantém os microsserviços desacoplados — o Notes Service não precisa saber nada sobre JWT.

@PatchMapping — atualização parcial (diferente do PUT que é completo)
@RequestParam — parâmetros de query string (?q=busca)
@PathVariable — parâmetros da URL (/notes/{id})
@PageableDefault — paginação automática do Spring Data (?page=0&size=10&sort=createdAt)
ResponseEntity.noContent() — retorna 204 No Content (padrão para DELETE)
*/
@RestController
@RequestMapping("/api/notes")
@Tag(name = "Notas", description = "CRUD de notas e grupos de notas")
public class NoteController {

    private static final Logger log = LoggerFactory.getLogger(NoteController.class);

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // ============================================================
    // Endpoints de Grupos
    // ============================================================

    @PostMapping("/groups")
    @Operation(summary = "Criar grupo de notas")
    public ResponseEntity<NoteGroupResponse> createGroup(
            @Valid @RequestBody CreateNoteGroupRequest request,
            @RequestHeader("X-User-Id") String userId) {
        NoteGroupResponse response = noteService.createGroup(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/groups")
    @Operation(summary = "Listar grupos do usuário")
    public ResponseEntity<List<NoteGroupResponse>> getGroups(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(noteService.getGroupsByUser(userId));
    }

    @DeleteMapping("/groups/{id}")
    @Operation(summary = "Deletar grupo e suas notas")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        noteService.deleteGroup(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // Endpoints de Notas
    // ============================================================

    @PostMapping
    @Operation(summary = "Criar nota")
    public ResponseEntity<NoteResponse> createNote(
            @Valid @RequestBody CreateNoteRequest request,
            @RequestHeader("X-User-Id") String userId) {
        NoteResponse response = noteService.createNote(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar notas do usuário")
    public ResponseEntity<List<NoteResponse>> getNotes(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(noteService.getNotesByUser(userId));
    }

    @GetMapping("/paginated")
    @Operation(summary = "Listar notas com paginação")
    public ResponseEntity<Page<NoteResponse>> getNotesPaginated(
            @RequestHeader("X-User-Id") String userId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(noteService.getNotesByUserPaginated(userId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar nota por ID")
    public ResponseEntity<NoteResponse> getNoteById(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(noteService.getNoteById(id, userId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar nota")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable String id,
            @Valid @RequestBody UpdateNoteRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(noteService.updateNote(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar nota")
    public ResponseEntity<Void> deleteNote(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        noteService.deleteNote(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar notas por texto")
    public ResponseEntity<List<NoteResponse>> searchNotes(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam String q) {
        return ResponseEntity.ok(noteService.searchNotes(userId, q));
    }

    @PatchMapping("/{id}/pin")
    @Operation(summary = "Fixar/desafixar nota")
    public ResponseEntity<NoteResponse> togglePin(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(noteService.togglePin(id, userId));
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Arquivar nota")
    public ResponseEntity<NoteResponse> archiveNote(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(noteService.archiveNote(id, userId));
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notes Service is running!");
    }
}