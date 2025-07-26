INSERT INTO owner (name, telephone) VALUES
('Bruce Wayne', '9876543210'),
('Clark Kent', '1023456789');

INSERT INTO pet (name, age, owner_id) VALUES
('Krypto', 3, 1),
('Betty', 2, 1),
('Luna', 4, 2);

INSERT INTO vaccine (pet_id, vaccination_date) VALUES
(1, '2025-07-23 10:00:00'),
(2, '2025-07-23 11:00:00'),
(3, '2025-07-23 12:00:00');

INSERT INTO users (username, password) VALUES
('admin', 'password@123');