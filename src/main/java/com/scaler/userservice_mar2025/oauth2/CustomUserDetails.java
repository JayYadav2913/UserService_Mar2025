package com.scaler.userservice_mar2025.oauth2;

import com.scaler.userservice_mar2025.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

//Entity that maps User object into UseDetails type Object.
public class CustomUserDetails implements UserDetails {

    private User user;

    public CustomUserDetails(User user) {
            this.user = user;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles()
                .stream()
                .map(CustomGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
