package org.ecommerce.v1.service;

import lombok.RequiredArgsConstructor;
import org.ecommerce.v1.dto.RequestDto.CreateReviewRequest;
import org.ecommerce.v1.dto.RequestDto.UpdateReviewRequest;
import org.ecommerce.v1.entity.Product;
import org.ecommerce.v1.entity.Review;
import org.ecommerce.v1.entity.User;
import org.ecommerce.v1.repository.ProductRepository;
import org.ecommerce.v1.repository.ReviewRepository;
import org.ecommerce.v1.repository.UserRepository;
import org.ecommerce.v1.utils.exceptions.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public Review createReview(CreateReviewRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User with ID " + request.getUserId() + " not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product with ID " + request.getProductId() + " not found"));

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review with ID " + reviewId + " not found"));
    }

    @Transactional(readOnly = true)
    public Page<Review> getAllReviewsPaged(
            int page,
            int size,
            String sortBy,
            String direction,
            Long productIdFilter,
            Long userIdFilter
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        if (productIdFilter != null) {
            return reviewRepository.findByProductId(productIdFilter, pageable);
        } else if (userIdFilter != null) {
            return reviewRepository.findByUserId(userIdFilter, pageable);
        }

        return reviewRepository.findAll(pageable);
    }

    public Review updateReview(Long reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review with ID " + reviewId + " not found"));

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        return review;
    }

    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review with ID " + reviewId + " not found"));
        reviewRepository.delete(review);
    }
}
