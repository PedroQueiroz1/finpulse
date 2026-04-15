package com.finpulse.auth.repository;

import com.finpulse.auth.entity.User;
import com.finpulse.auth.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/*
Isso é Spring Data JPA...
Só pelo nome do método o Spring já gera a implementação em tempo de execução. Já sabe o que fazer...
O extends JpaRepository já tem por padrão o save(), findById(), findAll(), delete(), count() e vários outros

OBS: JPQL (Java Persistence Query Language) é a linguagem de consulta do JPA. É parecida com SQL, mas em vez de consultar tabelas, você consulta entidades Java.
no JPQL você usa o nome da classe (User) e dos atributos (u.role, u.active), não o nome da tabela/coluna. 
O Hibernate traduz para SQL real. A vantagem é que se você mudar o nome da tabela no banco, o JPQL continua funcionando porque referencia a entidade Java.

Derived Query (pelo nome do método) — é o mais simples. Usar para consultas simples: findByEmail, existsByEmail, countByRole. 
É o mais legível, mas fica ilegível para queries complexas (imagina findByRoleAndActiveAndCreatedAtAfterOrderByNameAsc).

JPQL (@Query sem nativeQuery) — use para consultas de média complexidade. Vantagem: independente do banco 
(funciona em PostgreSQL, MySQL, Oracle, etc.). É o que você é usado em projetos reais.

Native Query (@Query com nativeQuery = true) — usar quando precisar de funcionalidades específicas do banco 
(funções PostgreSQL, window functions, CTEs). Desvantagem: amarra o código ao banco específico.

A anotação @Modifying é obrigatória em queries que alteram dados (UPDATE, DELETE) — avisa ao Spring que não é um SELECT.

Metodo criado por: Pedro Queiroz
Projeto de estudos
*/
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByActiveTrue();

    List<User> findByActiveFalse();

    long countByRole(Role role);

    // ============================================================
    // 2) JPQL — Consulta orientada a objetos (usa nomes de entidades/atributos)
    //    Mais flexível que derived queries, independente do banco
    // ============================================================

    // Busca usuários por role que estejam ativos
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.active = true")
    List<User> findActiveUsersByRole(@Param("role") Role role);

    // Busca usuários criados após uma data específica
    @Query("SELECT u FROM User u WHERE u.createdAt >= :startDate ORDER BY u.createdAt DESC")
    List<User> findUsersCreatedAfter(@Param("startDate") LocalDateTime startDate);

    // Busca por nome parcial (case-insensitive) — útil para busca/filtro
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> searchByName(@Param("name") String name);

    // Conta usuários ativos por role — exemplo de função agregadora em JPQL
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.active = true")
    long countActiveByRole(@Param("role") Role role);

    // Update via JPQL — desativa um usuário (soft delete)
    @Modifying
    @Query("UPDATE User u SET u.active = false, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    int deactivateUser(@Param("id") UUID id);

    // ============================================================
    // 3) NATIVE QUERY — SQL puro, quando precisa de algo específico do banco
    //    Útil para queries complexas ou funções específicas do PostgreSQL
    // ============================================================

    // Busca estatísticas de usuários por role usando SQL nativo
    @Query(
        value = "SELECT u.role, COUNT(*) as total, " +
                "SUM(CASE WHEN u.active THEN 1 ELSE 0 END) as active_count " +
                "FROM users u GROUP BY u.role",
        nativeQuery = true
    )
    List<Object[]> getUserStatsByRole();
}