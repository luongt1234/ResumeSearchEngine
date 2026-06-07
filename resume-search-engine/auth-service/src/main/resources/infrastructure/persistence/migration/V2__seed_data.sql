-- Seed dữ liệu Role mặc định
INSERT INTO roles (id, created_at, updated_at, name, description)
VALUES
    ('r-admin-uuid-0001', NOW(), NOW(), 'ROLE_ADMIN', 'Quản trị viên hệ thống'),
    ('r-candidate-uuid-0002', NOW(), NOW(), 'ROLE_CANDIDATE', 'Ứng viên/Người tìm việc');

-- Seed dữ liệu Permission mặc định
INSERT INTO permissions (id, created_at, updated_at, name, description)
VALUES
    ('p-cv-upload-0001', NOW(), NOW(), 'cv:upload', 'Quyền upload CV lên hệ thống'),
    ('p-cv-delete-0002', NOW(), NOW(), 'cv:delete', 'Quyền xóa CV');

-- Gán quyền cho các Role
INSERT INTO roles_permissions (role_id, permission_id)
VALUES
    ('r-candidate-uuid-0002', 'p-cv-upload-0001'), -- ROLE_CANDIDATE được upload CV
    ('r-admin-uuid-0001', 'p-cv-upload-0001'),
    ('r-admin-uuid-0001', 'p-cv-delete-0002');   -- ROLE_ADMIN được toàn quyền
