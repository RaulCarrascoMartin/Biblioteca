package com.example.demo.users.internal.persistence.repository.mapper;

import com.example.demo.users.internal.domain.model.Email;
import com.example.demo.users.internal.domain.model.Role;
import com.example.demo.users.internal.domain.model.User;
import com.example.demo.users.internal.persistence.dao.entity.UserEntity;
import org.springframework.stereotype.Component;

/** Convierte entre la entidad JPA y el modelo de dominio. Sin reglas de negocio. */
@Component
public class UserPersistenceMapper {

    /** Reconstruye el modelo con su constructor, de modo que el dominio vuelve a comprobar sus invariantes. */
    public User toModel(UserEntity entity) {
        return new User(entity.getId(), entity.getName(), new Email(entity.getEmail()), entity.getPassword(),
                Role.valueOf(entity.getRole()), entity.getTokenVersion());
    }

    /** Para modelos sin id; rechaza los que ya lo traen. */
    public UserEntity toNewEntity(User user) {
        if (user.id() != null) {
            throw new IllegalArgumentException("A new user cannot have an id");
        }
        return new UserEntity(user.name(), user.email().value(), user.passwordHash(), user.role().name(),
                user.tokenVersion());
    }

    /** Actualiza una entidad gestionada con el estado del modelo. */
    public void updateManagedEntity(User user, UserEntity entity) {
        entity.setName(user.name());
        entity.setEmail(user.email().value());
        entity.setPassword(user.passwordHash());
        entity.setRole(user.role().name());
        entity.setTokenVersion(user.tokenVersion());
    }
}
