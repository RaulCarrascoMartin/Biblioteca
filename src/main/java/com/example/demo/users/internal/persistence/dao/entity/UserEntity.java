package com.example.demo.users.internal.persistence.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/** Los nombres de tabla y columnas se mantienen explícitos para no cambiar el esquema (D16). */
@Entity
@Table(name = "usuarios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @Column(name = "nombre")
    private String name;

    @Setter
    @Column(name = "email", unique = true)
    private String email;

    @Setter
    @Column(name = "password")
    private String password;

    @Setter
    @Column(name = "rol")
    private String role;

    /** Se incrementa para invalidar todos los tokens emitidos antes (cambio de clave, cierre global). */
    @Setter
    @Column(name = "token_version", nullable = false)
    private int tokenVersion;

    public UserEntity(String name, String email, String password, String role, int tokenVersion) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.tokenVersion = tokenVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity other = (UserEntity) o;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "UserEntity{id=" + id + ", email='" + email + "'}";
    }
}
