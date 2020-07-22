DELETE from application_user;
INSERT INTO application_user(`id`, `email`, `password`) values
 (1, 'user1@test.com', 'encrypted_password1'),
 (2, 'user2@test.com', 'encrypted_password2'),
 (3, 'user3@test.com', 'encrypted_password3'),
 (4, 'user4@test.com', 'encrypted_password4');
