-- Script khởi tạo CSDL cho ứng dụng Task Management (Sprint 1)
-- Database Engine: MySQL 8.0+

CREATE DATABASE IF NOT EXISTS `taskmanagement_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `taskmanagement_db`;

-- Xóa bảng cũ nếu tồn tại (theo thứ tự ngược lại của ràng buộc khóa ngoại)
DROP TABLE IF EXISTS `invitations`;
DROP TABLE IF EXISTS `boards`;
DROP TABLE IF EXISTS `team_members`;
DROP TABLE IF EXISTS `teams`;
DROP TABLE IF EXISTS `users`;

-- 1. Bảng users (Người dùng)
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `display_name` VARCHAR(255) NOT NULL,
    `phone` VARCHAR(20) NULL,
    `password` VARCHAR(255) NOT NULL,
    `avatar_url` VARCHAR(255) NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng teams (Nhóm làm việc)
CREATE TABLE `teams` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `type` VARCHAR(255) NULL,
    `visibility` ENUM('PUBLIC', 'PRIVATE') NOT NULL,
    `description` TEXT NULL,
    `created_by` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_teams_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng team_members (Thành viên nhóm - Bảng trung gian User - Team)
CREATE TABLE `team_members` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `team_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `role` ENUM('ADMIN', 'MEMBER') NOT NULL,
    `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `uk_team_user` UNIQUE (`team_id`, `user_id`),
    CONSTRAINT `fk_team_members_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_team_members_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng boards (Bảng công việc)
CREATE TABLE `boards` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `team_id` BIGINT NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `created_by` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_boards_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_boards_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng invitations (Lời mời tham gia nhóm)
CREATE TABLE `invitations` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `team_id` BIGINT NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `role` ENUM('ADMIN', 'MEMBER') NOT NULL,
    `token` VARCHAR(255) NOT NULL UNIQUE,
    `status` ENUM('PENDING', 'ACCEPTED', 'EXPIRED') NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_invitations_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================================
-- DỮ LIỆU KHỞI TẠO MẶC ĐỊNH (DEFAULT SEED DATA)
-- ========================================================

-- 1. Tài khoản Admin mặc định
-- Email: admin@gmail.com
-- Mật khẩu: 123456 (Đã mã hóa BCrypt)
INSERT INTO `users` (`id`, `email`, `display_name`, `phone`, `password`, `avatar_url`)
VALUES (1, 'admin@gmail.com', 'Quản trị viên', '0912345678', '$2b$10$uFDWtTbFL00ipKmu74R3qedJj9vcDQBvpu6BlpA4dZhPHbGPPcTEm', NULL);

