package org.ecommerce.v1.security;

import org.ecommerce.v1.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;


public class OAuth2UserPrincipal implements OAuth2User {

    private final OAuth2User oauth2User;
    private final User user;

    public OAuth2UserPrincipal(OAuth2User oauth2User, User user) {
        this.oauth2User = oauth2User;
        this.user = user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Stream.of(user.getRole())
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .toList();
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    public User getUser() {
        return user;
    }

    public String getUsername() {
        return user.getUsername();
    }

    public Long getUserId() {
        return user.getId();
    }
}
