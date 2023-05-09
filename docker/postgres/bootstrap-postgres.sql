CREATE USER iwnet WITH PASSWORD '123456';

CREATE USER iwnet_admin WITH PASSWORD '123456';

ALTER USER iwnet_admin CREATEDB;

GRANT iwnet to iwnet_admin;

CREATE DATABASE bda_db WITH OWNER iwnet;

CREATE DATABASE test_db WITH OWNER iwnet;

CREATE USER keycloak WITH PASSWORD 'password';

CREATE DATABASE keycloak WITH OWNER keycloak;

GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;


\connect bda_db

CREATE TABLE db_info (
    id           SERIAL PRIMARY KEY,
    slug         VARCHAR(64) NOT NULL UNIQUE,
    name         VARCHAR(128) NOT NULL,
    description  VARCHAR(256),
    dbname       VARCHAR(64) NOT NULL UNIQUE,
    connector_id INTEGER
);

ALTER TABLE db_info OWNER TO iwnet;

CREATE TABLE execution_engines (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(64) NOT NULL UNIQUE,
    engine_path     TEXT,
    local_engine    BOOLEAN DEFAULT(true),
    args            JSONB
);

ALTER TABLE execution_engines OWNER TO iwnet;

CREATE TABLE execution_languages (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(64) NOT NULL UNIQUE
);

ALTER TABLE execution_languages OWNER TO iwnet;

CREATE TABLE connectors (
    id                 SERIAL PRIMARY KEY,
    name               VARCHAR(64) NOT NULL UNIQUE,
    address            VARCHAR(256) NOT NULL,
    port               VARCHAR(5) NOT NULL,
    metadata           JSON,
    is_external        BOOLEAN DEFAULT(false)
);

ALTER TABLE connectors OWNER TO iwnet;

CREATE TABLE shared_recipes (
    id                  SERIAL PRIMARY KEY,
    name                VARCHAR(64) NOT NULL UNIQUE,
    description         VARCHAR(256),
    language_id         INTEGER NOT NULL,
    executable_path     VARCHAR(512) NOT NULL,
    engine_id           INTEGER NOT NULL,
    args                JSON
    );

ALTER TABLE shared_recipes OWNER TO iwnet;

\connect test_db

CREATE TABLE db_info (
    id           SERIAL PRIMARY KEY,
    slug         VARCHAR(64) NOT NULL UNIQUE,
    name         VARCHAR(128) NOT NULL,
    description  VARCHAR(256),
    dbname       VARCHAR(64) NOT NULL UNIQUE,
    connector_id INTEGER
);

ALTER TABLE db_info OWNER TO iwnet;

CREATE TABLE execution_engines (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(64) NOT NULL UNIQUE,
    engine_path     TEXT,
    local_engine    BOOLEAN DEFAULT(true),
    args            JSONB
);

ALTER TABLE execution_engines OWNER TO iwnet;

CREATE TABLE execution_languages (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(64) NOT NULL UNIQUE
);

ALTER TABLE execution_languages OWNER TO iwnet;

CREATE TABLE connectors (
    id                 SERIAL PRIMARY KEY,
    name               VARCHAR(64) NOT NULL UNIQUE,
    address            VARCHAR(256) NOT NULL,
    port               VARCHAR(5) NOT NULL,
    metadata           JSON,
    is_external        BOOLEAN DEFAULT(false)
);

ALTER TABLE connectors OWNER TO iwnet;

CREATE TABLE shared_recipes (
    id                  SERIAL PRIMARY KEY,
    name                VARCHAR(64) NOT NULL UNIQUE,
    description         VARCHAR(256),
    language_id         INTEGER NOT NULL,
    executable_path     VARCHAR(512) NOT NULL,
    engine_id           INTEGER NOT NULL,
    args                JSON
    );

ALTER TABLE shared_recipes OWNER TO iwnet;
