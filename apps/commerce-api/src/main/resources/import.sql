-- Hibernate가 ddl-auto=create 또는 create-drop일 때 자동 실행
INSERT INTO brands (id, name_ko, name_en, cover_image_url, profile_image_url, created_at, updated_at) VALUES (1, '나이키', 'Nike', 'https://example.com/nike.jpg', 'https://example.com/nike-profile.jpg', NOW(), NOW());
INSERT INTO brands (id, name_ko, name_en, cover_image_url, profile_image_url, created_at, updated_at) VALUES (2, '아디다스', 'Adidas', 'https://example.com/adidas.jpg', 'https://example.com/adidas-profile.jpg', NOW(), NOW());

INSERT INTO member (id, user_id, name, email, birth, gender, created_at, updated_at) VALUES (1, 'test001', '테스트1', 'test1@test.com', '1990-01-01', 'M', NOW(), NOW());
INSERT INTO member (id, user_id, name, email, birth, gender, created_at, updated_at) VALUES (2, 'test002', '테스트2', 'test2@test.com', '1995-05-15', 'F', NOW(), NOW());

INSERT INTO products (id, brand_id, name_ko, price, description, status, release_year, shipping_fee, created_at, updated_at) VALUES (1, 1, '에어맥스', 150000, '인기상품', 'AVAILABLE', 2024, 3000, NOW(), NOW());
INSERT INTO products (id, brand_id, name_ko, price, description, status, release_year, shipping_fee, created_at, updated_at) VALUES (2, 2, '울트라부스트', 180000, '러닝화', 'AVAILABLE', 2024, 0, NOW(), NOW());

INSERT INTO product_stocks (id, product_id, stock, created_at, updated_at) VALUES (1, 1, 100, NOW(), NOW());
INSERT INTO product_stocks (id, product_id, stock, created_at, updated_at) VALUES (2, 2, 50, NOW(), NOW());
