/*
 * Copyright (C) 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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
