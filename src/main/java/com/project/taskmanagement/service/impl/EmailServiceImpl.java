package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    @Async
    public void sendTeamDeletionNotification(String teamName, List<String> memberEmails, String deleterName) {
        if (memberEmails == null || memberEmails.isEmpty()) {
            return;
        }

        String subject = "[TaskManagement] Thông báo: Nhóm '" + teamName + "' đã bị xóa";
        String body = String.format(
                "Xin chào,\n\nNhóm làm việc '%s' trên hệ thống TaskManagement vừa bị xóa bởi quản trị viên %s.\n" +
                        "Tất cả các bảng và dữ liệu liên quan đến nhóm này sẽ không còn hiển thị.\n\n" +
                        "Trân trọng,\nĐội ngũ TaskManagement",
                teamName,
                deleterName != null ? deleterName : "Quản trị viên");

        for (String email : memberEmails) {
            boolean sent = false;
            try {
                if (mailSender != null) {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(email);
                    message.setSubject(subject);
                    message.setText(body);
                    mailSender.send(message);
                    sent = true;
                    log.info("Đã gửi email thông báo xóa nhóm '{}' tới thành viên: {}", teamName, email);
                }
            } catch (Exception e) {
                log.warn("Không thể gửi email thông báo xóa nhóm qua SMTP server tới {}: {}", email, e.getMessage());
            }

            if (!sent) {
                log.info("\n========================================================================\n" +
                        "📢 [THÔNG BÁO XÓA NHÓM HỆ THỐNG]\n" +
                        "Gửi đến thành viên: {}\n" +
                        "Tên Nhóm đã xóa: {}\n" +
                        "Người xóa: {}\n" +
                        "========================================================================",
                        email, teamName, deleterName != null ? deleterName : "Quản trị viên");
            }
        }
    }

    @Override
    @Async
    public void sendTeamInvitationEmail(String toEmail, String teamName, String inviterName, String inviteUrl,
            String roleName) {
        String subject = "[TaskManagement] Lời mời tham gia nhóm '" + teamName + "'";
        String body = String.format(
                "Xin chào,\n\nBạn vừa nhận được lời mời tham gia nhóm '%s' trên TaskManagement với vai trò '%s' từ %s.\n\n"
                        +
                        "Vui lòng truy cập đường dẫn sau để chấp nhận lời mời:\n%s\n\n" +
                        "Trân trọng,\nĐội ngũ TaskManagement",
                teamName, roleName, inviterName, inviteUrl);

        boolean sent = false;
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                sent = true;
                log.info("Đã gửi email mời tham gia nhóm '{}' tới: {}", teamName, toEmail);
            }
        } catch (Exception e) {
            log.warn("Không thể gửi qua SMTP server: {}. Chuyển sang ghi log hệ thống.", e.getMessage());
        }

        if (!sent) {
            log.info("\n========================================================================\n" +
                    "📩 [THÔNG BÁO LỜI MỜI THAM GIA NHÓM]\n" +
                    "Đến: {}\n" +
                    "Nhóm: {}\n" +
                    "👉 Link chấp nhận lời mời: {}\n" +
                    "========================================================================",
                    toEmail, teamName, inviteUrl);
        }
    }

    @Override
    @Async
    public void sendBoardInvitationEmail(String toEmail, String boardName, String inviterName, String inviteUrl,
            String roleName) {
        String subject = "[TaskManagement] Lời mời tham gia bảng '" + boardName + "'";
        String body = String.format(
                "Xin chào,\n\nBạn vừa nhận được lời mời tham gia bảng công việc '%s' trên TaskManagement với vai trò '%s' từ %s.\n\n"
                        +
                        "Vui lòng truy cập đường dẫn sau để chấp nhận lời mời:\n%s\n\n" +
                        "Trân trọng,\nĐội ngũ TaskManagement",
                boardName, roleName, inviterName, inviteUrl);

        boolean sent = false;
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                sent = true;
                log.info("Đã gửi email mời tham gia bảng '{}' tới: {}", boardName, toEmail);
            }
        } catch (Exception e) {
            log.warn("Không thể gửi qua SMTP server: {}. Chuyển sang ghi log hệ thống.", e.getMessage());
        }

        if (!sent) {
            log.info("\n========================================================================\n" +
                    "📩 [THÔNG BÁO LỜI MỜI THAM GIA BẢNG]\n" +
                    "Đến: {}\n" +
                    "Bảng: {}\n" +
                    "👉 Link chấp nhận lời mời: {}\n" +
                    "========================================================================",
                    toEmail, boardName, inviteUrl);
        }
    }
}
