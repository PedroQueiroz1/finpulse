package com.finpulse.notes.repository;

import com.finpulse.notes.document.NoteGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
MongoDB Query vs JPQL: Perceba que as queries do MongoDB usam JSON syntax em vez de SQL. {'userId': ?0, 'tags': {$in: ?1}} é MongoDB query language 
— $in busca documentos onde o array tags contém qualquer valor da lista passada. 
Isso é algo que SQL faz com JOINs e tabelas intermediárias, mas MongoDB resolve em uma linha porque arrays são cidadãos de primeira classe.
 */
@Repository
public interface NoteGroupRepository extends MongoRepository<NoteGroup, String> {

    List<NoteGroup> findByUserIdOrderByNameAsc(String userId);

    boolean existsByNameAndUserId(String name, String userId);
}