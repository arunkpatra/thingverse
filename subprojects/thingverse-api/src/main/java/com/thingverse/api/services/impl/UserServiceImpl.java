package com.thingverse.api.services.impl;

import com.thingverse.api.entity.UserEntity;
import com.thingverse.api.repository.UserRepository;
import com.thingverse.api.services.UserService;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;

//@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Cacheable("userByUsername")
    public Optional<UserEntity> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
