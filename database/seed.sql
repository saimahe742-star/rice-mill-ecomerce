-- Product seed data (matches DataInitializer). Run on an empty `products` table, e.g. after schema.sql.
-- If products already exist, skip or truncate first (dev only).

USE rice_mill_db;

INSERT INTO products (name, price, image_url) VALUES
('Coconut Oil 200ml', 100.00, '/images/sample-oil.svg'),
('Coconut Oil 500ml', 220.00, '/images/sample-oil.svg'),
('Groundnut Oil 1L', 250.00, '/images/sample-oil.svg'),
('Groundnut Oil 2L', 500.00, '/images/sample-oil.svg'),
('Groundnut Oil 5L', 1250.00, '/images/sample-oil.svg'),
('Sesame Oil 500ml', 190.00, '/images/sample-oil.svg'),
('Sesame Oil 1L', 390.00, '/images/sample-oil.svg'),
('Rice — Z Royal 26kg', 1750.00, '/images/sample-rice.svg'),
('Rice — Z Gold 26kg', 1700.00, '/images/sample-rice.svg'),
('Rice — Malligai 26kg', 1700.00, '/images/sample-rice.svg'),
('Rice — Kathir 26kg', 1700.00, '/images/sample-rice.svg'),
('Rice — Nellai 26kg', 1600.00, '/images/sample-rice.svg'),
('Rice — Thenmalar 26kg', 1550.00, '/images/sample-rice.svg'),
('Rice — Roja 26kg', 1500.00, '/images/sample-rice.svg'),
('Rice — Raja Gopuram 26kg', 1400.00, '/images/sample-rice.svg'),
('Rice — Triple Horse 26kg', 1350.00, '/images/sample-rice.svg'),
('Rice — Madurai Gate 26kg', 1300.00, '/images/sample-rice.svg'),
('Rice — Cow Brand 26kg', 1250.00, '/images/sample-rice.svg'),
('Rice — Sivaji 26kg', 1200.00, '/images/sample-rice.svg'),
('Rice — Muyal 26kg', 1150.00, '/images/sample-rice.svg'),
('Rice — Sugam New', 1100.00, '/images/sample-rice.svg'),
('Rice — Muthamizh New 75kg', 3700.00, '/images/sample-rice.svg'),
('Rice — Mahaveeran New 75kg', 4500.00, '/images/sample-rice.svg'),
('Rice — Veera Sivaji 10kg', 600.00, '/images/sample-rice.svg'),
('Rice — Veera Sivaji 5kg', 300.00, '/images/sample-rice.svg'),
('Rice — Bullet 10kg', 580.00, '/images/sample-rice.svg'),
('Rice — Bullet 5kg', 290.00, '/images/sample-rice.svg'),
('Rice — Ulundhu 25kg', 3250.00, '/images/sample-rice.svg'),
('Rice — Paruppu 25kg', 3000.00, '/images/sample-rice.svg');

-- Users with BCrypt passwords are seeded by the app (DataInitializer) on first run.
-- To load only products without starting the app, run schema.sql then this file.
