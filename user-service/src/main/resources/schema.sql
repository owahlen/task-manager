CREATE TABLE IF NOT EXISTS application_user (
    id serial primary key,
    email varchar(320) UNIQUE,
    password varchar(200)
);
