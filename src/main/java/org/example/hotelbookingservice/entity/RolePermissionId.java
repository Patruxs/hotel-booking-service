package org.example.hotelbookingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Embeddable
public class RolePermissionId implements Serializable {
    private static final long serialVersionUID = 2585344907146630961L;

    @NotNull
    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @NotNull
    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        RolePermissionId entity = (RolePermissionId) o;
        return Objects.equals(this.roleId, entity.roleId)
                && Objects.equals(this.permissionId, entity.permissionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, permissionId);
    }
}
