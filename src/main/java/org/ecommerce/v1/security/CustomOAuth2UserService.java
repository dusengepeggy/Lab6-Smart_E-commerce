package org.ecommerce.v1.security;

import lombok.RequiredArgsConstructor;
import org.ecommerce.v1.entity.Role;
import org.ecommerce.v1.entity.User;
import org.ecommerce.v1.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(request);
        String provider = request.getClientRegistration().getRegistrationId();
        String subject = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        User user = userRepository.findByOauth2ProviderAndOauth2Subject(provider, subject)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(email != null ? email : "oauth_" + subject);
                    newUser.setEmail(email != null ? email : "oauth_" + subject + "@oauth.local");
                    newUser.setPassword(null);
                    newUser.setRole(Role.CUSTOMER);
                    newUser.setOauth2Provider(provider);
                    newUser.setOauth2Subject(subject);
                    if (userRepository.existsByUsername(newUser.getUsername())) {
                        newUser.setUsername("oauth_" + subject + "_" + UUID.randomUUID().toString().substring(0, 8));
                    }
                    if (userRepository.existsByEmail(newUser.getEmail())) {
                        newUser.setEmail("oauth_" + subject + "@oauth.local");
                    }
                    return userRepository.save(newUser);
                });

        return new OAuth2UserPrincipal(oauth2User, user);
    }
}
