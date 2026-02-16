package org.ecommerce.v1.service;

import lombok.RequiredArgsConstructor;
import org.ecommerce.v1.entity.Order;
import org.ecommerce.v1.entity.Review;
import org.ecommerce.v1.repository.OrderRepository;
import org.ecommerce.v1.repository.ReviewRepository;
import org.ecommerce.v1.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service("securityService")
@RequiredArgsConstructor
public class SecurityService {

    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    public boolean isCurrentUser(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails details)) {
            return false;
        }
        return details.getUserId().equals(userId);
    }

    public boolean isOrderOwner(Long orderId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) return false;
        return orderRepository.findById(orderId)
                .map(Order::getUser)
                .map(u -> u.getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isReviewOwner(Long reviewId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) return false;
        return reviewRepository.findById(reviewId)
                .map(Review::getUser)
                .map(u -> u.getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_STAFF".equals(a.getAuthority()));
    }

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUserId();
        }
        return null;
    }
}
