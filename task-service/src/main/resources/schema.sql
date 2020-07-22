CREATE TABLE IF NOT EXISTS task (
    id serial primary key,
    description varchar(200),
    completed boolean
);
