package org.ecommerce.v1.utils.exceptions;


public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
