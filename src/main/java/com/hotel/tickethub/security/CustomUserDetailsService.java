package com.hotel.tickethub.security;

import com.hotel.tickethub.model.User;
import com.hotel.tickethub.repository.UserRepository;
import com.hotel.tickethub.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Get roles from repository to avoid LazyInitializationException
        // Use same logic as AuthService with fallbacks
        com.hotel.tickethub.model.UserRole userRole = userRoleRepository
                .findByUserId(user.getId())
                .or(() -> userRoleRepository.findByUserIdCustom(user.getId()))
                .or(() -> userRoleRepository.findByUserIdNative(user.getId()))
                .orElse(null);
        
        List<org.springframework.security.core.GrantedAuthority> authorities;
        
        if (userRole != null) {
            // Use role found via repository
            authorities = List.of(new SimpleGrantedAuthority("ROLE_" + userRole.getRole().name()));
        } else {
            // Fallback: use User.userRoles relation (requires @Transactional)
            // Initialize lazy collection explicitly
            if (user.getUserRoles() != null && !user.getUserRoles().isEmpty()) {
                authorities = user.getUserRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRole().name()))
                        .collect(Collectors.toList());
            } else {
                // No role found - use default role
                authorities = List.of(new SimpleGrantedAuthority("ROLE_CLIENT"));
            }
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}
