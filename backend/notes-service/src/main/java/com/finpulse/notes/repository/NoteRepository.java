package com.finpulse.notes.repository;

import com.finpulse.notes.document.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/*
MongoDB Query vs JPQL: Perceba que as queries do MongoDB usam JSON syntax em vez de SQL. {'userId': ?0, 'tags': {$in: ?1}} é MongoDB query language 
— $in busca documentos onde o array tags contém qualquer valor da lista passada. 
Isso é algo que SQL faz com JOINs e tabelas intermediárias, mas MongoDB resolve em uma linha porque arrays são cidadãos de primeira classe.
 */
@Repository
public interface NoteRepository extends MongoRepository<Note, String> {

    // ============================================================
    // Derived Queries (mesmo conceito do JPA, funciona no MongoDB)
    // ============================================================

    List<Note> findByUserIdAndArchivedFalseOrderByPinnedDescCreatedAtDesc(String userId);

    List<Note> findByUserIdAndGroupId(String userId, String groupId);

    List<Note> findByUserIdAndArchivedTrue(String userId);

    long countByUserIdAndGroupId(String userId, String groupId);

    // ============================================================
    // MongoDB Query Language (equivalente ao JPQL, mas para MongoDB)
    // Usa JSON query syntax do MongoDB
    // ============================================================

    // Busca notas por tags (MongoDB suporta queries em arrays nativamente!)
    @Query("{'userId': ?0, 'tags': {$in: ?1}, 'archived': false}")
    List<Note> findByUserIdAndTagsIn(String userId, List<String> tags);

    // Busca notas fixadas do usuário
    @Query("{'userId': ?0, 'pinned': true, 'archived': false}")
    List<Note> findPinnedByUserId(String userId);

    // Busca com paginação — essencial para performance
    @Query("{'userId': ?0, 'archived': false}")
    Page<Note> findActiveNotesByUserId(String userId, Pageable pageable);

    // Full-text search no conteúdo das notas
    @Query("{'userId': ?0, '$text': {'$search': ?1}}")
    List<Note> searchByContent(String userId, String searchTerm);

    // Busca notas criadas em um período
    @Query("{'userId': ?0, 'createdAt': {$gte: ?1, $lte: ?2}}")
    List<Note> findByUserIdAndCreatedAtBetween(
            String userId, LocalDateTime start, LocalDateTime end);
}