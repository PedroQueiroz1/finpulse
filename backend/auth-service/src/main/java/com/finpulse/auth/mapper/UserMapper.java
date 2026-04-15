package com.finpulse.auth.mapper;

import com.finpulse.auth.dto.UserResponse;
import com.finpulse.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/*
MapStruct: Você declara uma interface com um método toResponse(User user) e o MapStruct gera a implementação automaticamente em tempo de compilação. 
Ele faz o match pelos nomes dos campos - como User tem id, name, email, role, active e o Record UserResponse também, ele sabe exatamente o que mapear. 
Se um campo tivesse nome diferente, usaríamos @Mapping(source = "nomeEntidade", target = "nomeDto").
O componentModel = "spring" faz o mapper ser um bean do Spring — você pode injetá-lo com @Autowired ou via construtor.

Metodo criado por Pedro Queiroz
Projeto de estudos
*/
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserResponse toResponse(User user);
}