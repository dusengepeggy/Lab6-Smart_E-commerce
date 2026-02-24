package org.ecommerce.v1.repository;

import org.ecommerce.v1.entity.Role;
import org.ecommerce.v1.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<User> findByRole(Role role, Pageable pageable);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
