package com.thingverse.api.repository;


import com.thingverse.api.entity.AuthorityEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AuthorityRepository extends CrudRepository<AuthorityEntity, Long> {
    @Transactional(readOnly = true)
    List<AuthorityEntity> findAllByUserNameIgnoreCase(String userName);
}
