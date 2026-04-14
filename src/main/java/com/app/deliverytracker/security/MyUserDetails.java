package com.app.deliverytracker.security;

import com.app.deliverytracker.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

public record MyUserDetails(User user) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
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
    public User user() {
        return user;
    }
//    @Override
//    public boolean isAccountNonLocked() {
//        return user.getStatus() != UserStatus.BLOCKED;
//    }
//
//
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }
}
