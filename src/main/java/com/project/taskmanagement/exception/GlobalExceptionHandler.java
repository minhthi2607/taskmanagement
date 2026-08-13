package com.project.taskmanagement.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý ngoại lệ không tìm thấy tài nguyên (404)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFoundException(ResourceNotFoundException ex, Model model, HttpServletRequest request) {
        log.warn("ResourceNotFoundException at {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    /**
     * Xử lý lỗi không có quyền truy cập (SecurityException / AccessDeniedException)
     */
    @ExceptionHandler({SecurityException.class, AccessDeniedException.class})
    public String handleAccessDeniedException(Exception ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.warn("AccessDeniedException at {}: {}", request.getRequestURI(), ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage() != null ? ex.getMessage() : "Bạn không có quyền thực hiện thao tác này!");
        String referer = request.getHeader("Referer");
        return (referer != null && !referer.isBlank()) ? "redirect:" + referer : "redirect:/team/list";
    }

    /**
     * Xử lý các ngoại lệ tham số không hợp lệ (IllegalArgumentException)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.warn("IllegalArgumentException at {}: {}", request.getRequestURI(), ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        String referer = request.getHeader("Referer");
        return (referer != null && !referer.isBlank()) ? "redirect:" + referer : "redirect:/";
    }

    /**
     * Xử lý lỗi upload file vượt dung lượng cho phép
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.warn("MaxUploadSizeExceededException at {}: {}", request.getRequestURI(), ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", "Dung lượng file upload vượt quá giới hạn cho phép (tối đa 5MB)!");
        String referer = request.getHeader("Referer");
        return (referer != null && !referer.isBlank()) ? "redirect:" + referer : "redirect:/user/profile";
    }

    /**
     * Xử lý các lỗi vi phạm ràng buộc dữ liệu CSDL (DataIntegrityViolationException - ví dụ: trùng lặp dữ liệu unique)
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public String handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.warn("DataIntegrityViolationException at {}: {}", request.getRequestURI(), ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu không hợp lệ hoặc đã tồn tại trong hệ thống (vi phạm ràng buộc dữ liệu)!");
        String referer = request.getHeader("Referer");
        return (referer != null && !referer.isBlank()) ? "redirect:" + referer : "redirect:/";
    }

    /**
     * Xử lý lỗi validate form dữ liệu đầu vào (BindException / MethodArgumentNotValidException)
     */
    @ExceptionHandler(org.springframework.validation.BindException.class)
    public String handleBindException(org.springframework.validation.BindException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.warn("BindException at {}: {}", request.getRequestURI(), ex.getMessage());
        String errorMsg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        redirectAttributes.addFlashAttribute("errorMessage", errorMsg != null ? errorMsg : "Dữ liệu nhập vào không hợp lệ!");
        String referer = request.getHeader("Referer");
        return (referer != null && !referer.isBlank()) ? "redirect:" + referer : "redirect:/";
    }

    /**
     * Xử lý ngoại lệ trạng thái ứng dụng không hợp lệ (IllegalStateException)
     */
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.warn("IllegalStateException at {}: {}", request.getRequestURI(), ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage() != null ? ex.getMessage() : "Thao tác không thể thực hiện ở trạng thái hiện tại!");
        String referer = request.getHeader("Referer");
        return (referer != null && !referer.isBlank()) ? "redirect:" + referer : "redirect:/";
    }

    /**
     * Xử lý tĩnh cho các tài nguyên không tồn tại (ví dụ: /favicon.ico hoặc tệp tĩnh thiếu)
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public org.springframework.http.ResponseEntity<Void> handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex, HttpServletRequest request) {
        log.debug("No static resource found at: {}", request.getRequestURI());
        return org.springframework.http.ResponseEntity.notFound().build();
    }

    /**
     * Bắt tất cả ngoại lệ không lường trước (Catch-all 500)
     */
    @ExceptionHandler(Exception.class)
    public Object handleGenericException(Exception ex, Model model, HttpServletRequest request) {
        if ("/favicon.ico".equals(request.getRequestURI())) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
        log.error("Unhandled Exception at {}: ", request.getRequestURI(), ex);
        model.addAttribute("errorMessage", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau!");
        return "error/500";
    }
}
