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