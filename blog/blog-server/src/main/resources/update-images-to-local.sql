-- Update user avatars to use local images
UPDATE users SET avatar = 'http://localhost:8080/api/files/42cd7d67-f8ff-4a46-87c9-6af4ca0bd6a5.webp' WHERE id = 2;
UPDATE users SET avatar = 'http://localhost:8080/api/files/5316183f-c28b-4ff5-b4ee-b1bd76644749.webp' WHERE id = 3;
UPDATE users SET avatar = 'http://localhost:8080/api/files/83fbc060-a8c3-4037-8417-027e3dbb3dad.webp' WHERE id = 4;
UPDATE users SET avatar = 'http://localhost:8080/api/files/87118651-2ca1-45ba-821c-c11594fb6def.webp' WHERE id = 5;
UPDATE users SET avatar = 'http://localhost:8080/api/files/87553653-2c3a-4d74-a93c-53283e2552ec.webp' WHERE id = 6;

-- Update article cover images to use local images (first 10 articles)
UPDATE articles SET cover_image = 'http://localhost:8080/api/files/8cc602f5-3658-4fbd-a4c7-e495443d36c1.webp' WHERE id = 1;
UPDATE articles SET cover_image = 'http://localhost:8080/api/files/931f1275-da98-458c-b125-3000e5fa9cab.webp' WHERE id = 2;
UPDATE articles SET cover_image = 'http://localhost:8080/api/files/a9d1f3f6-3adf-4095-a8da-cf7f78f6df6f.webp' WHERE id = 3;
UPDATE articles SET cover_image = 'http://localhost:8080/api/files/bf94f474-50ed-4945-9d3d-3809efae1587.webp' WHERE id = 4;
UPDATE articles SET cover_image = 'http://localhost:8080/api/files/f66d635c-422f-4e53-971e-c74cda8118e6.webp' WHERE id = 5;
UPDATE articles SET cover_image = 'http://localhost:8080/api/files/42cd7d67-f8ff-4a46-87c9-6af4ca0bd6a5.webp' WHERE id = 6;
UPDATE articles SET cover_image = 'http://localhost:8080/api/files/5316183f-c28b-4ff5-b4ee-b1bd76644749.webp' WHERE id = 7;
UPDATE articles SET cover_image = 'http://localhost:8080/api/files/83fbc060-a8c3-4037-8417-027e3dbb3dad.webp' WHERE id = 8;
UPDATE articles SET cover_image = 'http://localhost:8080/api/files/87118651-2ca1-45ba-821c-c11594fb6def.webp' WHERE id = 9;
UPDATE articles SET cover_image = 'http://localhost:8080/api/files/87553653-2c3a-4d74-a93c-53283e2552ec.webp' WHERE id = 10;
