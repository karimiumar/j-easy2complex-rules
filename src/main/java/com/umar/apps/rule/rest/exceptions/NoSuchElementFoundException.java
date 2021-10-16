package com.umar.apps.rule.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * An exception to denote HTTP Status Code 404.
 * {@link NoSuchElementFoundException} is thrown when a request is
 * received for an element which is non-existent
 *
 * @author Mohammad Umar Ali Karimi (karimiumar@gmail.com)
 */
public class NoSuchElementFoundException extends ResponseStatusException {

    /**
     * Constructor for {@link NoSuchElementFoundException}
     *
     * @param reason The reason to be used
     */
    public NoSuchElementFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }
}
