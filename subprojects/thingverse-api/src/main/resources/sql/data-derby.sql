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

INSERT INTO user_table (user_name, password)
VALUES ('dummy_user', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6'),
       ('thingverse', '$2a$10$NAUza3ggBMYYKOr5UB3QPe3WLGDcIrcJQ4arKxdLLB0f/VM.7HxVu'),
       ('bad_user', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6');

INSERT INTO role_table (role_name)
VALUES ('ROLE_USER'),
       ('ROLE_ADMIN');

INSERT INTO authority_table (user_name, authority)
VALUES ('dummy_user', 'ROLE_USER'),
       ('thingverse', 'ROLE_USER'),
       ('thingverse', 'ROLE_ADMIN');