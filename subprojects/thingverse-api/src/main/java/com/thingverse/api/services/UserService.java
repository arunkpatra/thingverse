package com.thingverse.api.services;

import com.thingverse.api.entity.UserEntity;

import java.util.Optional;

public interface UserService {

    Optional<UserEntity> getUserByUsername(String username);

}
