-- Script khởi tạo CSDL cho ứng dụng Task Management (Sprint 1 + Sprint 2)
-- Database Engine: MySQL 8.0+

CREATE DATABASE IF NOT EXISTS `taskmanagement_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `taskmanagement_db`;

-- Xóa bảng cũ nếu tồn tại (theo thứ tự ngược lại của ràng buộc khóa ngoại)
DROP TABLE IF EXISTS `invitations`;
DROP TABLE IF EXISTS `card_time_logs`;
DROP TABLE IF EXISTS `card_watchers`;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `card_comments`;
DROP TABLE IF EXISTS `card_attachments`;
DROP TABLE IF EXISTS `card_members`;
DROP TABLE IF EXISTS `card_labels`;
DROP TABLE IF EXISTS `cards`;
DROP TABLE IF EXISTS `labels`;
DROP TABLE IF EXISTS `task_lists`;
DROP TABLE IF EXISTS `board_members`;
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
-- [Sprint 2] Bổ sung cột `visibility` (GEMINI.md mục 6.4)
CREATE TABLE `boards` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `team_id` BIGINT NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `visibility` ENUM('PRIVATE', 'GROUP', 'PUBLIC') NOT NULL,
    `created_by` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_boards_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_boards_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng board_members (Thành viên bảng - Bảng trung gian User - Board) [Sprint 2 - MỚI]
-- GEMINI.md mục 6.5
CREATE TABLE `board_members` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `board_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `role` ENUM('ADMIN', 'MEMBER') NOT NULL,
    `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `uk_board_user` UNIQUE (`board_id`, `user_id`),
    CONSTRAINT `fk_board_members_board` FOREIGN KEY (`board_id`) REFERENCES `boards` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_board_members_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Bảng task_lists (Danh sách công việc - cột trong Board) [Sprint 2 - MỚI]
-- GEMINI.md mục 6.6
CREATE TABLE `task_lists` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `board_id` BIGINT NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `position` INT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_task_lists_board` FOREIGN KEY (`board_id`) REFERENCES `boards` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Bảng labels (Kho nhãn dùng chung của Board) [Sprint 2 - MỚI]
-- GEMINI.md mục 6.8
CREATE TABLE `labels` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `board_id` BIGINT NOT NULL,
    `name` VARCHAR(255) NULL,
    `color` VARCHAR(20) NULL,
    CONSTRAINT `fk_labels_board` FOREIGN KEY (`board_id`) REFERENCES `boards` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Bảng cards (Thẻ công việc) [Sprint 2 - MỚI]
-- GEMINI.md mục 6.7
CREATE TABLE `cards` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_list_id` BIGINT NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT NULL,
    `position` INT NULL,
    `created_by` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL,
    CONSTRAINT `fk_cards_task_list` FOREIGN KEY (`task_list_id`) REFERENCES `task_lists` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_cards_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Bảng card_labels (Bảng trung gian Card - Label) [Sprint 2 - MỚI]
-- GEMINI.md mục 6.9
CREATE TABLE `card_labels` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `card_id` BIGINT NOT NULL,
    `label_id` BIGINT NOT NULL,
    CONSTRAINT `uk_card_label` UNIQUE (`card_id`, `label_id`),
    CONSTRAINT `fk_card_labels_card` FOREIGN KEY (`card_id`) REFERENCES `cards` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_card_labels_label` FOREIGN KEY (`label_id`) REFERENCES `labels` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Bảng card_members (Thành viên được gán vào thẻ) [Sprint 2 - MỚI]
-- GEMINI.md mục 6.10
CREATE TABLE `card_members` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `card_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    CONSTRAINT `uk_card_user` UNIQUE (`card_id`, `user_id`),
    CONSTRAINT `fk_card_members_card` FOREIGN KEY (`card_id`) REFERENCES `cards` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_card_members_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Bảng card_attachments (File đính kèm) [Sprint 2 - MỚI]
-- GEMINI.md mục 6.11
CREATE TABLE `card_attachments` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `card_id` BIGINT NOT NULL,
    `file_url` VARCHAR(500) NOT NULL,
    `file_name` VARCHAR(255) NOT NULL,
    `uploaded_by` BIGINT NOT NULL,
    `uploaded_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_card_attachments_card` FOREIGN KEY (`card_id`) REFERENCES `cards` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_card_attachments_uploaded_by` FOREIGN KEY (`uploaded_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Bảng card_comments (Bình luận trong thẻ) [Sprint 2 - MỚI]
-- GEMINI.md mục 6.12
CREATE TABLE `card_comments` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `card_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `content` TEXT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_card_comments_card` FOREIGN KEY (`card_id`) REFERENCES `cards` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_card_comments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Bảng invitations (Lời mời tham gia Team hoặc Board)
-- [Sprint 2] Bổ sung cột `board_id` (nullable), nới `team_id` thành nullable — dùng chung
-- cho cả 2 luồng mời (GEMINI.md mục 6.13). Đúng 1 trong 2 cột team_id/board_id phải có giá trị,
-- ràng buộc bằng CHECK constraint bên dưới làm lớp bảo vệ cuối cùng ở tầng DB (validate chính vẫn
-- ở tầng Service khi tạo Invitation).
CREATE TABLE `invitations` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `team_id` BIGINT NULL,
    `board_id` BIGINT NULL,
    `email` VARCHAR(255) NOT NULL,
    `role` ENUM('ADMIN', 'MEMBER') NOT NULL,
    `token` VARCHAR(255) NOT NULL UNIQUE,
    `status` ENUM('PENDING', 'ACCEPTED', 'EXPIRED') NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_invitations_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_invitations_board` FOREIGN KEY (`board_id`) REFERENCES `boards` (`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_invitation_target` CHECK (
        (`team_id` IS NOT NULL AND `board_id` IS NULL) OR
        (`team_id` IS NULL AND `board_id` IS NOT NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================================
-- SPRINT 3 — Thông báo, theo dõi thẻ, nhật ký thời gian, due date (GEMINI.md mục 14)
-- ========================================================

-- 14. Bảng notifications (Thông báo trong ứng dụng) [Sprint 3 - MỚI]
-- GEMINI.md mục 14.1
CREATE TABLE `notifications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `type` ENUM('CARD_ADDED', 'CARD_MOVED', 'CARD_MEMBER_ASSIGNED', 'BOARD_MEMBER_ADDED', 'TEAM_MEMBER_ADDED', 'CARD_DUE_REMINDER', 'CARD_WATCH_ACTIVITY') NOT NULL,
    `content` TEXT NOT NULL,
    `link` VARCHAR(255) NULL,
    `is_read` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. Bảng card_watchers (Theo dõi thẻ - #54) [Sprint 3 - MỚI]
-- GEMINI.md mục 14.1
CREATE TABLE `card_watchers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `card_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `uk_card_watcher_user` UNIQUE (`card_id`, `user_id`),
    CONSTRAINT `fk_card_watchers_card` FOREIGN KEY (`card_id`) REFERENCES `cards` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_card_watchers_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. Bảng card_time_logs (Nhật ký thời gian đã dùng - #55) [Sprint 3 - MỚI]
-- GEMINI.md mục 14.1
CREATE TABLE `card_time_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `card_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `hours` DECIMAL(5, 2) NOT NULL,
    `note` VARCHAR(255) NULL,
    `logged_at` DATETIME NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_card_time_logs_card` FOREIGN KEY (`card_id`) REFERENCES `cards` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_card_time_logs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. Bảng cards — bổ sung cột due date + nhắc việc (#53) [Sprint 3]
-- GEMINI.md mục 14.2
ALTER TABLE `cards`
    ADD COLUMN `due_date` DATETIME NULL,
    ADD COLUMN `reminder_minutes` INT NULL,
    ADD COLUMN `reminder_sent_at` DATETIME NULL;

-- ========================================================
-- DỮ LIỆU KHỞI TẠO MẶC ĐỊNH (DEFAULT SEED DATA)
-- ========================================================

-- 1. Tài khoản Admin mặc định
-- Email: admin@gmail.com
-- Mật khẩu: 123456 (Đã mã hóa BCrypt)
INSERT INTO `users` (`id`, `email`, `display_name`, `phone`, `password`, `avatar_url`)
VALUES (1, 'admin@gmail.com', 'Quản trị viên', '0912345678', '$2b$10$uFDWtTbFL00ipKmu74R3qedJj9vcDQBvpu6BlpA4dZhPHbGPPcTEm', NULL);