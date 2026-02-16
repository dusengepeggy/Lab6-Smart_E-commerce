package org.ecommerce.v1.service;

import lombok.RequiredArgsConstructor;
import org.ecommerce.v1.entity.Role;
import org.ecommerce.v1.utils.exceptions.NotFoundException;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ecommerce.v1.entity.User;
import org.ecommerce.v1.repository.UserRepository;
import org.ecommerce.v1.dto.RequestDto.RegisterRequest;
import org.ecommerce.v1.dto.RequestDto.UpdateUserRequest;
import org.ecommerce.v1.dto.ResponseDto.UserDTO;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @CacheEvict(value = "users", allEntries = true)
    public UserDTO register(RegisterRequest request) {

        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(hashedPassword);
        user.setRole(request.getRole() != null ? request.getRole() : Role.CUSTOMER);

        userRepository.save(user);

        return convertToDto(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getUsersPaged(
            int page,
            int size,
            String sortBy,
            String direction,
            String usernameFilter,
            String emailFilter,
            Role roleFilter
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users;
        if (usernameFilter != null && !usernameFilter.isBlank()) {
            users = userRepository.findByUsernameContainingIgnoreCase(usernameFilter, pageable);
        } else if (emailFilter != null && !emailFilter.isBlank()) {
            users = userRepository.findByEmailContainingIgnoreCase(emailFilter, pageable);
        } else if (roleFilter != null) {
            users = userRepository.findByRole(roleFilter, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#userId")
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found"));
        return convertToDto(user);
    }

    @CacheEvict(value = "users", key = "#userId")
    public UserDTO updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found"));

        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getRole() != null) user.setRole(request.getRole());

        return convertToDto(user);
    }

    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found"));
        userRepository.delete(user);
    }

    private UserDTO convertToDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }
}
