package com.nutriguideai.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    /**
     * Constructs a 404 exception with structured details about what was not found.
     *
     * @param resourceName the name of the resource that was not found (e.g. "User", "MealPlan")
     * @param fieldName    the field used for the lookup (e.g. "id", "email")
     * @param fieldValue   the value that was searched for (e.g. 99L, "john@test.com")
     *
     * Example usage:
     *   throw new ResourceNotFoundException("User", "id", 99L);
     *   // → "User not found with id: 99"
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /**
     * Simplified constructor for cases where only a message is needed.
     *
     * Example usage:
     *   throw new ResourceNotFoundException("No meal plans found for user 99");
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }
}
