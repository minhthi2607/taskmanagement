package com.project.taskmanagement.config;

import com.project.taskmanagement.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final User user; // dùng user.getId() ở Controller/Service để lấy user đang đăng nhập

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Quyền Quản trị nhóm / Thành viên là quyền RIÊNG theo từng Team,
        // không phải role toàn hệ thống -> ở đây chỉ cần 1 quyền chung là đã đăng nhập.
        // Muốn biết user có phải Quản trị của 1 nhóm cụ thể không,
        // gọi TeamMemberRepository.findByTeamIdAndUserId(...) trong Service.
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
