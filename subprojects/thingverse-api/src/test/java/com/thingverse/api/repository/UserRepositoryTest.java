package com.thingverse.api.repository;

import com.thingverse.api.AbstractTest;
import com.thingverse.api.entity.UserEntity;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserRepositoryTest extends AbstractTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void checkRead() {
        Optional<UserEntity> ue = userRepository.findByUsername("dummy_user");
        assertTrue(ue.isPresent());
        assertEquals("dummy_user", ue.get().getUsername());

        ue.get().getAuthorities();
    }

    @Rollback
    public void checkWrite() {
        UserEntity ue = new UserEntity();
        ue.setUsername("johndoe");
        userRepository.save(ue);
        assertTrue(userRepository.findByUsername("johndoe").isPresent());
    }
}
