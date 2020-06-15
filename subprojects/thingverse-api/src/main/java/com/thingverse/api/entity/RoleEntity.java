package com.thingverse.api.entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by M1020513 on 12/29/2016.
 */
@Entity
@Table(name = "role_table")
public class RoleEntity extends AbstractPersistable<Long> {

    @Column(name = "role_name")
    private String roleName;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
