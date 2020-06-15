package com.thingverse.api.repository;

import com.thingverse.api.entity.RoleEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends CrudRepository<RoleEntity, Long> {
    RoleEntity findOneByRoleNameIgnoreCase(String roleName);
}
