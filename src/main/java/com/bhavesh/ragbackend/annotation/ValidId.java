package com.bhavesh.ragbackend.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;


@Documented
@Constraint(validatedBy = {})
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "must contain only letters, digits, or underscores"
)
public @interface ValidId {
    String message() default "must contain only letters, digits, or underscores";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}