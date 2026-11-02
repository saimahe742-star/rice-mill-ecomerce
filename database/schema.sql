-- Modern Rice Mill — reference schema (Hibernate `ddl-auto=update` can create/update tables automatically).
-- Create database first:
CREATE DATABASE IF NOT EXISTS rice_mill_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE rice_mill_db;

-- Optional: run these if you prefer managing schema manually (then set spring.jpa.hibernate.ddl-auto=none).

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(180) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  price DECIMAL(12,2) NOT NULL,
  image_url VARCHAR(500) NULL
);

CREATE TABLE IF NOT EXISTS orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  total_price DECIMAL(12,2) NOT NULL,
  payment_method VARCHAR(10) NOT NULL,
  item_received BIT(1) NOT NULL DEFAULT 0,
  created_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
  CONSTRAINT fk_oi_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE IF NOT EXISTS feedback (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NULL,
  order_id BIGINT NOT NULL,
  rating INT NOT NULL,
  comments VARCHAR(2000) NULL,
  return_reason VARCHAR(1000) NULL,
  CONSTRAINT fk_fb_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_fb_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);
