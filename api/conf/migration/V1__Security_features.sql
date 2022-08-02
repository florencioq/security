CREATE SCHEMA security;

CREATE TABLE security.users(
    id SERIAL PRIMARY KEY,

    email VARCHAR NOT NULL,
    password VARCHAR NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE security.applications(
    key VARCHAR(16) PRIMARY KEY,
    name VARCHAR NOT NULL,
    webapp_url VARCHAR
);

CREATE TABLE security.user_app_links(
    user_id INT NOT NULL REFERENCES security.users(id),
    app_key VARCHAR(16) NOT NULL REFERENCES security.applications(key),
    disabled BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY(user_id, app_key)
);

CREATE TABLE security.roles(
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL UNIQUE,
    app_key VARCHAR(16) NOT NULL REFERENCES security.applications(key)
);

CREATE TABLE security.permissions(
    user_id INT NOT NULL REFERENCES security.users(id),
    role_id INT NOT NULL REFERENCES security.roles(id),
    PRIMARY KEY (user_id, role_id)
);


CREATE TABLE security.admins(
    user_id INT PRIMARY KEY REFERENCES security.users(id)
);

CREATE TABLE security.managers(
    user_id INT NOT NULL REFERENCES security.users(id),
    app_key VARCHAR(16) NOT NULL REFERENCES security.applications(key),
    PRIMARY KEY (user_id, app_key)
);


CREATE TABLE security.password_redefinitions(
    user_id int NOT NULL REFERENCES security.users(id),
    redefinition_uuid uuid NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, redefinition_uuid)
);


INSERT INTO security.users VALUES (0, 'admin@ideos.com.br', '$2a$10$9CYesUmpNA2izblDPuSwQ.rKM2phGlOwLgFvnB/aQptLzmeMWoyKu');

INSERT INTO security.applications VALUES ('5FA9F74DD1A12657', 'Partech platform');
INSERT INTO security.applications VALUES ('CFE6AA49A7929AC2', 'Geo Net');

INSERT INTO security.user_app_links VALUES (0, '5FA9F74DD1A12657');

INSERT INTO security.admins VALUES (0);
INSERT INTO security.managers VALUES (0, '5FA9F74DD1A12657');

