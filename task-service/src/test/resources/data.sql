-- noinspection SqlWithoutWhereForFile

DELETE FROM item_tag;
DELETE FROM tag;
DELETE FROM item;
DELETE FROM users;

INSERT INTO users (id, first_name, last_name) VALUES
    (1, 'Richard', 'Countin'),
    (2, 'Nathalie', 'Queen'),
    (3, 'Benito', 'Corazon'),
    (4, 'Vince', 'Power');

INSERT INTO tag (id, name) VALUES
    (1, 'Work'),
    (2, 'Private'),
    (3, 'Meeting'),
    (4, 'Sport'),
    (5, 'Meal'),
    (6, 'Drink'),
    (7, 'Vacation');

INSERT INTO item (id, description, assignee_id, status) VALUES
    (1, 'Flight to JNB', 1, 'TODO'),
    (2, 'Organise an celebration for the Great Place to Work', 2, 'TODO'),
    (3, 'Organise a drink with the team', 3, 'IN_PROGRESS'),
    (4, 'Meet Ms Cosmic', 4, 'DONE');

INSERT INTO item_tag (item_id, tag_id) VALUES
    (1, 2),
    (1, 7),
    (2, 1),
    (2, 3),
    (2, 5),
    (2, 6),
    (3, 1),
    (3, 6),
    (4, 2);

