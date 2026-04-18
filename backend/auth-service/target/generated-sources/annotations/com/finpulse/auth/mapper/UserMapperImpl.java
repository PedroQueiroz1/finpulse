package com.finpulse.auth.mapper;

import com.finpulse.auth.dto.UserResponse;
import com.finpulse.auth.entity.User;
import com.finpulse.auth.enums.Role;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-18T12:09:56-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UUID id = null;
        String name = null;
        String email = null;
        Role role = null;
        boolean active = false;

        id = user.getId();
        name = user.getName();
        email = user.getEmail();
        role = user.getRole();
        active = user.isActive();

        UserResponse userResponse = new UserResponse( id, name, email, role, active );

        return userResponse;
    }
}
