package com.finpulse.auth.repository;

import com.finpulse.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/*
Isso é Spring Data JPA...
Só pelo nome do método o Spring já gera a implementação em tempo de execução. Já sabe o que fazer...
O extends JpaRepository já tem por padrão o save(), findById(), findAll(), delete(), count() e vários outros
*/
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}