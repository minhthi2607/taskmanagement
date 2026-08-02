package com.project.taskmanagement.config;

import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Cắt khoảng trắng thừa (.trim()) và đổi về chữ thường (.toLowerCase())
        String cleanEmail = email != null ? email.trim().toLowerCase() : "";

        User user = userRepository.findByEmail(cleanEmail)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Không tìm thấy tài khoản với email: " + cleanEmail));

        return new UserPrincipal(user);
    }
}
